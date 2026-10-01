package com.colegiosapiens.servico;

import com.colegiosapiens.dto.AlunoDto;
import com.colegiosapiens.excepcao.RecursoNaoEncontradoException;
import com.colegiosapiens.excepcao.RegraNegocioException;
import com.colegiosapiens.modelo.Aluno;
import com.colegiosapiens.modelo.Turma;
import com.colegiosapiens.repositorio.AlunoRepositorio;
import com.colegiosapiens.repositorio.TurmaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** US27: cadastro e consulta de alunos. */
@Service
public class AlunoServico {

    private final AlunoRepositorio repositorio;
    private final TurmaRepositorio turmaRepositorio;
    private final UtilizadorServico utilizadorServico;

    public AlunoServico(AlunoRepositorio repositorio, TurmaRepositorio turmaRepositorio,
                        UtilizadorServico utilizadorServico) {
        this.repositorio = repositorio;
        this.turmaRepositorio = turmaRepositorio;
        this.utilizadorServico = utilizadorServico;
    }

    @Transactional
    public AlunoDto.Resposta cadastrar(AlunoDto.Requisicao dados) {
        Turma turma = turmaRepositorio.findById(dados.turmaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turma não encontrada."));

        String codigo = dados.codigoEstudante().trim();
        if (repositorio.existsByCodigoEstudante(codigo)) {
            throw new RegraNegocioException("Já existe um aluno com este código de estudante.");
        }

        // A senha é opcional: sem ela, o aluno existe mas não tem acesso à plataforma.
        String senha = (dados.senha() == null || dados.senha().isBlank()) ? null : dados.senha();

        Aluno aluno = new Aluno();
        utilizadorServico.preencherContaBase(aluno, dados.nomeCompleto(),
                dados.email(), dados.telefone(), senha);
        aluno.setCodigoEstudante(codigo);
        aluno.setDataNascimento(dados.dataNascimento());
        aluno.setTurma(turma);

        return AlunoDto.Resposta.de(repositorio.save(aluno));
    }

    /** Altera a situação do aluno. Inativo e transferido deixam de poder iniciar sessão. */
    @Transactional
    public AlunoDto.Resposta alterarSituacao(Long id, String situacao) {
        Aluno aluno = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado."));
        aluno.setSituacao(situacao);
        aluno.setAtivo("ATIVO".equals(situacao));
        return AlunoDto.Resposta.de(aluno);
    }

    @Transactional(readOnly = true)
    public List<AlunoDto.Resposta> listar() {
        return repositorio.findAll().stream().map(AlunoDto.Resposta::de).toList();
    }
}
