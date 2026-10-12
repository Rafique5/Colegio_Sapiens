package com.colegiosapiens.servico;

import com.colegiosapiens.dto.AlunoDto;
import com.colegiosapiens.dto.NotaDto;
import com.colegiosapiens.dto.SolicitacaoDto;
import com.colegiosapiens.excepcao.RecursoNaoEncontradoException;
import com.colegiosapiens.excepcao.RegraNegocioException;
import com.colegiosapiens.modelo.Aluno;
import com.colegiosapiens.modelo.Nota;
import com.colegiosapiens.modelo.Professor;
import com.colegiosapiens.modelo.SolicitacaoReuniao;
import com.colegiosapiens.modelo.VinculoParental;
import com.colegiosapiens.repositorio.AlocacaoDocenteRepositorio;
import com.colegiosapiens.repositorio.AlunoRepositorio;
import com.colegiosapiens.repositorio.NotaRepositorio;
import com.colegiosapiens.repositorio.ProfessorRepositorio;
import com.colegiosapiens.repositorio.SolicitacaoReuniaoRepositorio;
import com.colegiosapiens.repositorio.VinculoParentalRepositorio;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Portal do encarregado (educandos, notas, notificações) e os lançamentos que o alimentam. */
@Service
public class AcompanhamentoServico {

    private final VinculoParentalRepositorio vinculos;
    private final NotaRepositorio notas;
    private final AlunoRepositorio alunos;
    private final ProfessorRepositorio professores;
    private final SolicitacaoReuniaoRepositorio solicitacoes;
    private final AlocacaoDocenteRepositorio alocacoes;

    public AcompanhamentoServico(VinculoParentalRepositorio vinculos, NotaRepositorio notas,
                                 AlunoRepositorio alunos, ProfessorRepositorio professores,
                                 SolicitacaoReuniaoRepositorio solicitacoes,
                                 AlocacaoDocenteRepositorio alocacoes) {
        this.alocacoes = alocacoes;
        this.vinculos = vinculos;
        this.notas = notas;
        this.alunos = alunos;
        this.professores = professores;
        this.solicitacoes = solicitacoes;
    }

    /** Educandos associados à conta do encarregado. */
    @Transactional(readOnly = true)
    public List<AlunoDto.Resposta> educandos(Long encarregadoId) {
        return vinculos.findByEncarregadoIdAndAtivoTrue(encarregadoId).stream()
                .map(v -> AlunoDto.Resposta.de(v.getAluno()))
                .toList();
    }

    /** Pauta de um educando por disciplina. Um encarregado só vê os seus próprios educandos. */
    @Transactional(readOnly = true)
    public List<NotaDto.Linha> notasDoEducando(Long encarregadoId, Long alunoId, int trimestre) {
        boolean permitido = vinculos.findByEncarregadoIdAndAtivoTrue(encarregadoId).stream()
                .anyMatch(v -> v.getAluno().getId().equals(alunoId));
        if (!permitido) {
            throw new RecursoNaoEncontradoException("Educando não encontrado.");
        }
        Map<String, Double[]> porDisciplina = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Nota n : notas.findByAlunoIdAndTrimestre(alunoId, trimestre)) {
            Double[] testes = porDisciplina.computeIfAbsent(n.getDisciplina(), k -> new Double[2]);
            testes["Teste 2".equals(n.getAvaliacao()) ? 1 : 0] = n.getValor();
        }
        List<NotaDto.Linha> linhas = new ArrayList<>();
        for (Map.Entry<String, Double[]> e : porDisciplina.entrySet()) {
            Double t1 = e.getValue()[0];
            Double t2 = e.getValue()[1];
            double soma = 0;
            int quantas = 0;
            if (t1 != null) { soma += t1; quantas++; }
            if (t2 != null) { soma += t2; quantas++; }
            double media = Math.round(soma / quantas * 10.0) / 10.0;
            String situacao = media >= 11 ? "Positiva" : media >= 10 ? "Em risco" : "Negativa";
            linhas.add(new NotaDto.Linha(e.getKey(), t1, t2, media, situacao));
        }
        return linhas;
    }

    /** Lança (ou corrige) uma nota. */
    @Transactional
    public void lancarNota(NotaDto.Requisicao dados) {
        Aluno aluno = alunos.findById(dados.alunoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado."));
        String disciplina = dados.disciplina().trim();
        Nota nota = notas.findByAlunoIdAndDisciplinaIgnoreCaseAndAvaliacaoAndTrimestre(
                        aluno.getId(), disciplina, dados.avaliacao(), dados.trimestre())
                .orElseGet(() -> new Nota(aluno, disciplina, dados.avaliacao(), dados.trimestre(), dados.valor()));
        nota.setValor(dados.valor());
        notas.save(nota);
    }

    /** Cria um pedido de reunião para cada encarregado activo do aluno. Devolve quantos foram criados. */
    @Transactional
    public int solicitarReuniao(Long professorId, SolicitacaoDto.Requisicao dados, boolean soDasSuasTurmas) {
        Professor professor = professores.findById(professorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado."));
        Aluno aluno = alunos.findById(dados.alunoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado."));
        if (soDasSuasTurmas && !alocacoes.existsByProfessorIdAndTurmaId(professorId, aluno.getTurma().getId())) {
            throw new RecursoNaoEncontradoException("Aluno não encontrado.");
        }
        List<VinculoParental> ligacoes = vinculos.findByAlunoIdAndAtivoTrue(aluno.getId());
        if (ligacoes.isEmpty()) {
            throw new RegraNegocioException("O aluno ainda não tem encarregado associado.");
        }
        for (VinculoParental v : ligacoes) {
            solicitacoes.save(new SolicitacaoReuniao(professor, aluno, v.getEncarregado(),
                    dados.motivo().trim(), dados.dataSugerida()));
        }
        return ligacoes.size();
    }

    /** Todos os pedidos (visão da secretaria), do mais recente para o mais antigo. */
    @Transactional(readOnly = true)
    public List<SolicitacaoDto.Resposta> todasAsSolicitacoes() {
        return solicitacoes.findAll(Sort.by(Sort.Direction.DESC, "criadaEm")).stream()
                .map(SolicitacaoDto.Resposta::de)
                .toList();
    }

    /** Pedidos recebidos pelo encarregado, do mais recente para o mais antigo. */
    @Transactional(readOnly = true)
    public List<SolicitacaoDto.Resposta> notificacoes(Long encarregadoId) {
        return solicitacoes.findByEncarregadoIdOrderByCriadaEmDesc(encarregadoId).stream()
                .map(SolicitacaoDto.Resposta::de)
                .toList();
    }

    /** Abrir um pedido regista a visualização. */
    @Transactional
    public SolicitacaoDto.Resposta abrirNotificacao(Long encarregadoId, Long id) {
        SolicitacaoReuniao pedido = solicitacoes.findById(id)
                .filter(s -> s.getEncarregado().getId().equals(encarregadoId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada."));
        pedido.marcarComoLida();
        return SolicitacaoDto.Resposta.de(pedido);
    }
}
