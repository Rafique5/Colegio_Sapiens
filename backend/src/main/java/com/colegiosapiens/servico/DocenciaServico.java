package com.colegiosapiens.servico;

import com.colegiosapiens.dto.AlunoDto;
import com.colegiosapiens.dto.DocenciaDto;
import com.colegiosapiens.excepcao.RecursoNaoEncontradoException;
import com.colegiosapiens.excepcao.RegraNegocioException;
import com.colegiosapiens.modelo.AlocacaoDocente;
import com.colegiosapiens.modelo.Aluno;
import com.colegiosapiens.modelo.Assiduidade;
import com.colegiosapiens.modelo.Professor;
import com.colegiosapiens.modelo.Turma;
import com.colegiosapiens.repositorio.AlocacaoDocenteRepositorio;
import com.colegiosapiens.repositorio.AlunoRepositorio;
import com.colegiosapiens.repositorio.AssiduidadeRepositorio;
import com.colegiosapiens.repositorio.ProfessorRepositorio;
import com.colegiosapiens.repositorio.TurmaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Turmas dos professores, os seus alunos e o registo de assiduidade. */
@Service
public class DocenciaServico {

    private final AlocacaoDocenteRepositorio alocacoes;
    private final ProfessorRepositorio professores;
    private final TurmaRepositorio turmas;
    private final AlunoRepositorio alunos;
    private final AssiduidadeRepositorio assiduidade;

    public DocenciaServico(AlocacaoDocenteRepositorio alocacoes, ProfessorRepositorio professores,
                           TurmaRepositorio turmas, AlunoRepositorio alunos, AssiduidadeRepositorio assiduidade) {
        this.alocacoes = alocacoes;
        this.professores = professores;
        this.turmas = turmas;
        this.alunos = alunos;
        this.assiduidade = assiduidade;
    }

    // ---------- Administração: associar professores a turmas ----------

    @Transactional
    public DocenciaDto.AlocacaoResposta alocar(DocenciaDto.AlocacaoRequisicao dados) {
        Professor professor = professores.findById(dados.professorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado."));
        Turma turma = turmas.findById(dados.turmaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turma não encontrada."));
        if (alocacoes.existsByProfessorIdAndTurmaId(professor.getId(), turma.getId())) {
            throw new RegraNegocioException("Esse professor já está associado a essa turma.");
        }
        return paraResposta(alocacoes.save(new AlocacaoDocente(professor, turma)));
    }

    @Transactional(readOnly = true)
    public List<DocenciaDto.AlocacaoResposta> listarAlocacoes() {
        return alocacoes.findAll().stream().map(this::paraResposta).toList();
    }

    @Transactional
    public void removerAlocacao(Long id) {
        if (!alocacoes.existsById(id)) {
            throw new RecursoNaoEncontradoException("Associação não encontrada.");
        }
        alocacoes.deleteById(id);
    }

    // ---------- Professor ----------

    @Transactional(readOnly = true)
    public List<DocenciaDto.TurmaSimples> turmasDoProfessor(Long professorId) {
        return alocacoes.findByProfessorId(professorId).stream()
                .map(a -> new DocenciaDto.TurmaSimples(a.getTurma().getId(), a.getTurma().getNomeTurma()))
                .sorted(Comparator.comparing(DocenciaDto.TurmaSimples::nome))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AlunoDto.Resposta> alunosDoProfessor(Long professorId) {
        return alocacoes.findByProfessorId(professorId).stream()
                .flatMap(a -> alunos.findByTurmaId(a.getTurma().getId()).stream())
                .map(AlunoDto.Resposta::de)
                .sorted(Comparator.comparing(AlunoDto.Resposta::nomeTurma)
                        .thenComparing(AlunoDto.Resposta::nomeCompleto))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocenciaDto.AlunoAssiduidade> assiduidade(Long professorId, Long turmaId, LocalDate data) {
        verificarTurma(professorId, turmaId);
        Map<Long, Boolean> marcas = assiduidade.findByAlunoTurmaIdAndDataAula(turmaId, data).stream()
                .collect(Collectors.toMap(a -> a.getAluno().getId(), Assiduidade::isPresente));
        return alunos.findByTurmaId(turmaId).stream()
                .sorted(Comparator.comparing(Aluno::getNomeCompleto))
                .map(a -> new DocenciaDto.AlunoAssiduidade(a.getId(), a.getNomeCompleto(), a.getCodigoEstudante(),
                        marcas.containsKey(a.getId()) ? (marcas.get(a.getId()) ? "PRESENTE" : "FALTA") : null))
                .toList();
    }

    /** Guarda (ou corrige) a presença/falta de cada aluno da turma nesse dia. Devolve quantos registos gravou. */
    @Transactional
    public int registarAssiduidade(Long professorId, DocenciaDto.AssiduidadeRequisicao dados) {
        verificarTurma(professorId, dados.turmaId());
        if (dados.data().isAfter(LocalDate.now())) {
            throw new RegraNegocioException("Não pode registar assiduidade numa data futura.");
        }
        Set<Long> daTurma = new HashSet<>();
        for (Aluno a : alunos.findByTurmaId(dados.turmaId())) {
            daTurma.add(a.getId());
        }
        int gravados = 0;
        for (DocenciaDto.Marca marca : dados.marcas()) {
            if (!daTurma.contains(marca.alunoId())) {
                throw new RegraNegocioException("Há um aluno que não pertence a esta turma.");
            }
            Assiduidade registo = assiduidade.findByAlunoIdAndDataAula(marca.alunoId(), dados.data())
                    .orElseGet(() -> new Assiduidade(alunos.getReferenceById(marca.alunoId()),
                            dados.data(), marca.presente()));
            registo.setPresente(marca.presente());
            assiduidade.save(registo);
            gravados++;
        }
        return gravados;
    }

    /** Um professor só mexe nas suas turmas (o erro não revela se a turma existe). */
    private void verificarTurma(Long professorId, Long turmaId) {
        if (!alocacoes.existsByProfessorIdAndTurmaId(professorId, turmaId)) {
            throw new RecursoNaoEncontradoException("Turma não encontrada.");
        }
    }

    private DocenciaDto.AlocacaoResposta paraResposta(AlocacaoDocente a) {
        return new DocenciaDto.AlocacaoResposta(a.getId(), a.getProfessor().getId(),
                a.getProfessor().getNomeCompleto(), a.getTurma().getId(), a.getTurma().getNomeTurma());
    }
}
