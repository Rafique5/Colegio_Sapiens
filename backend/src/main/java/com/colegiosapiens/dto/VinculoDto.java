package com.colegiosapiens.dto;

import com.colegiosapiens.modelo.VinculoParental;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Objectos de transferência de dados da associação aluno - encarregado (US11). */
public interface VinculoDto {

    record Requisicao(@NotNull Long alunoId, @NotNull Long encarregadoId,
                      @jakarta.validation.constraints.Size(max = 30) String parentesco) {
    }

    record Resposta(Long id, Long alunoId, String nomeAluno,
                    Long encarregadoId, String nomeEncarregado,
                    LocalDate dataAssociacao, boolean ativo, String parentesco) {

        public static Resposta de(VinculoParental v) {
            return new Resposta(v.getId(),
                    v.getAluno().getId(), v.getAluno().getNomeCompleto(),
                    v.getEncarregado().getId(), v.getEncarregado().getNomeCompleto(),
                    v.getDataAssociacao(), v.isAtivo(), v.getParentesco());
        }
    }
}
