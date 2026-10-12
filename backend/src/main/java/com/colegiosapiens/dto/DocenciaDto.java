package com.colegiosapiens.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** Docência: turmas dos professores e registo de assiduidade. */
public final class DocenciaDto {

    private DocenciaDto() {
    }

    public record AlocacaoRequisicao(@NotNull Long professorId, @NotNull Long turmaId) {
    }

    public record AlocacaoResposta(Long id, Long professorId, String nomeProfessor, Long turmaId, String nomeTurma) {
    }

    public record TurmaSimples(Long id, String nome) {
    }

    public record Marca(@NotNull Long alunoId, boolean presente) {
    }

    public record AssiduidadeRequisicao(@NotNull Long turmaId, @NotNull LocalDate data,
                                        @NotNull @Valid List<Marca> marcas) {
    }

    /** estado: PRESENTE, FALTA ou nulo (ainda sem registo nesse dia). */
    public record AlunoAssiduidade(Long alunoId, String nomeAluno, String codigo, String estado) {
    }
}
