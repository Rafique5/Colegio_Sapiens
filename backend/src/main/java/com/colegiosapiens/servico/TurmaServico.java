package com.colegiosapiens.servico;

import com.colegiosapiens.dto.AlunoDto;
import com.colegiosapiens.dto.TurmaDto;
import com.colegiosapiens.excepcao.RecursoNaoEncontradoException;
import com.colegiosapiens.excepcao.RegraNegocioException;
import com.colegiosapiens.modelo.Turma;
import com.colegiosapiens.repositorio.AlunoRepositorio;
import com.colegiosapiens.repositorio.TurmaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** US28: cadastro e consulta de turmas. */
@Service
public class TurmaServico {

    private final TurmaRepositorio repositorio;
    private final AlunoRepositorio alunoRepositorio;

    public TurmaServico(TurmaRepositorio repositorio, AlunoRepositorio alunoRepositorio) {
        this.repositorio = repositorio;
        this.alunoRepositorio = alunoRepositorio;
    }

    @Transactional
    public TurmaDto.Resposta cadastrar(TurmaDto.Requisicao dados) {
        String nome = dados.nomeTurma().trim();
        if (repositorio.existsByNomeTurmaAndAnoLectivo(nome, dados.anoLectivo())) {
            throw new RegraNegocioException("Já existe uma turma com este nome neste ano lectivo.");
        }

        Turma turma = new Turma();
        turma.setNomeTurma(nome);
        turma.setAnoLectivo(dados.anoLectivo());
        turma.setSala(dados.sala());
        return TurmaDto.Resposta.de(repositorio.save(turma));
    }

    @Transactional(readOnly = true)
    public List<TurmaDto.Resposta> listar() {
        return repositorio.findAll().stream().map(TurmaDto.Resposta::de).toList();
    }

    /** Lista os alunos de uma turma. */
    @Transactional(readOnly = true)
    public List<AlunoDto.Resposta> listarAlunos(Long turmaId) {
        if (!repositorio.existsById(turmaId)) {
            throw new RecursoNaoEncontradoException("Turma não encontrada.");
        }
        return alunoRepositorio.findByTurmaId(turmaId).stream().map(AlunoDto.Resposta::de).toList();
    }
}
