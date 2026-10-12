package com.colegiosapiens.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados de entrada e saída das notas. */
public final class NotaDto {

    private NotaDto() {
    }

    /** Lançamento de uma nota (escala 0 a 20). */
    public record Requisicao(
            @NotNull Long alunoId,
            @NotBlank @Size(max = 80) String disciplina,
            @NotBlank @Pattern(regexp = "Teste 1|Teste 2", message = "deve ser Teste 1 ou Teste 2") String avaliacao,
            @NotNull @DecimalMin("0") @DecimalMax("20") Double valor,
            @NotNull @Min(1) @Max(3) Integer trimestre) {
    }

    /** Uma linha da pauta: uma disciplina com os testes, a média parcial e a situação. */
    public record Linha(String disciplina, Double teste1, Double teste2, Double mediaParcial, String situacao) {
    }
}
