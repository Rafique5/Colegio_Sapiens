package com.colegiosapiens.servico;

import com.colegiosapiens.dto.VinculoDto;
import com.colegiosapiens.excepcao.RecursoNaoEncontradoException;
import com.colegiosapiens.excepcao.RegraNegocioException;
import com.colegiosapiens.modelo.Aluno;
import com.colegiosapiens.modelo.EncarregadoEducacao;
import com.colegiosapiens.modelo.VinculoParental;
import com.colegiosapiens.repositorio.AlunoRepositorio;
import com.colegiosapiens.repositorio.EncarregadoRepositorio;
import com.colegiosapiens.repositorio.VinculoParentalRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** US11: associa cada aluno ao respectivo encarregado de educação. */
@Service
public class VinculoServico {

    private final VinculoParentalRepositorio repositorio;
    private final AlunoRepositorio alunoRepositorio;
    private final EncarregadoRepositorio encarregadoRepositorio;

    public VinculoServico(VinculoParentalRepositorio repositorio,
                          AlunoRepositorio alunoRepositorio,
                          EncarregadoRepositorio encarregadoRepositorio) {
        this.repositorio = repositorio;
        this.alunoRepositorio = alunoRepositorio;
        this.encarregadoRepositorio = encarregadoRepositorio;
    }

    @Transactional
    public VinculoDto.Resposta associar(VinculoDto.Requisicao dados) {
        Aluno aluno = alunoRepositorio.findById(dados.alunoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno não encontrado."));
        EncarregadoEducacao encarregado = encarregadoRepositorio.findById(dados.encarregadoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encarregado não encontrado."));

        // Critério de aceitação: verificar se a associação já existe antes de criar outra.
        if (repositorio.existsByAlunoIdAndEncarregadoId(aluno.getId(), encarregado.getId())) {
            throw new RegraNegocioException("Este aluno já está associado a este encarregado.");
        }

        VinculoParental vinculo = new VinculoParental(aluno, encarregado);
        if (dados.parentesco() != null && !dados.parentesco().isBlank()) {
            vinculo.setParentesco(dados.parentesco().trim());
        }
        return VinculoDto.Resposta.de(repositorio.save(vinculo));
    }

    /** Todas as associações activas. */
    @Transactional(readOnly = true)
    public List<VinculoDto.Resposta> listarTodos() {
        return repositorio.findByAtivoTrue().stream().map(VinculoDto.Resposta::de).toList();
    }

    /** Educandos de um encarregado. */
    @Transactional(readOnly = true)
    public List<VinculoDto.Resposta> listarPorEncarregado(Long encarregadoId) {
        if (!encarregadoRepositorio.existsById(encarregadoId)) {
            throw new RecursoNaoEncontradoException("Encarregado não encontrado.");
        }
        return repositorio.findByEncarregadoIdAndAtivoTrue(encarregadoId).stream()
                .map(VinculoDto.Resposta::de).toList();
    }

    /** Encarregados de um aluno. */
    @Transactional(readOnly = true)
    public List<VinculoDto.Resposta> listarPorAluno(Long alunoId) {
        if (!alunoRepositorio.existsById(alunoId)) {
            throw new RecursoNaoEncontradoException("Aluno não encontrado.");
        }
        return repositorio.findByAlunoIdAndAtivoTrue(alunoId).stream()
                .map(VinculoDto.Resposta::de).toList();
    }
}
