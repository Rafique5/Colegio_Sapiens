package com.colegiosapiens.dto;

import com.colegiosapiens.modelo.Turma;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Objectos de transferência de dados das turmas (US28). */
public interface TurmaDto {

    record Requisicao(
            @NotBlank @Size(max = 50) String nomeTurma,
            @Min(2000) @Max(2100) int anoLectivo,
            @Size(max = 50) String sala) {
    }

    record Resposta(Long id, String nomeTurma, int anoLectivo, String sala) {

        public static Resposta de(Turma t) {
            return new Resposta(t.getId(), t.getNomeTurma(), t.getAnoLectivo(), t.getSala());
        }
    }
}
