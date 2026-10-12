package com.colegiosapiens.dto;

import com.colegiosapiens.modelo.SolicitacaoReuniao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Dados de entrada e saída dos pedidos de reunião. */
public final class SolicitacaoDto {

    private SolicitacaoDto() {
    }

    /** O professorId só é usado quando quem pede é o administrador; um professor pede sempre em seu nome. */
    public record Requisicao(
            Long professorId,
            @NotNull Long alunoId,
            @NotBlank @Size(max = 255) String motivo,
            @NotNull LocalDate dataSugerida) {
    }

    public record Resposta(Long id, String nomeProfessor, String nomeAluno, String motivo,
                           LocalDate dataSugerida, String estado, LocalDateTime criadaEm,
                           String nomeEncarregado) {

        public static Resposta de(SolicitacaoReuniao s) {
            return new Resposta(s.getId(), s.getProfessor().getNomeCompleto(), s.getAluno().getNomeCompleto(),
                    s.getMotivo(), s.getDataSugerida(), s.getEstado(), s.getCriadaEm(),
                    s.getEncarregado().getNomeCompleto());
        }
    }
}
