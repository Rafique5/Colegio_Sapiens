package com.colegiosapiens.dto;

import com.colegiosapiens.modelo.SincronizacaoSga;

import java.time.LocalDateTime;
import java.util.List;

/** Dados da tela de sincronização com o SGA. */
public final class SgaDto {

    private SgaDto() {
    }

    public record Registo(Long id, LocalDateTime dataHora, int registos, int novos, int alterados,
                          String estado, String mensagem) {

        public static Registo de(SincronizacaoSga s) {
            return new Registo(s.getId(), s.getDataHora(), s.getRegistos(), s.getNovos(), s.getAlterados(),
                    s.getEstado(), s.getMensagem());
        }
    }

    public record Resumo(long alunos, long turmas, long professores, long encarregados, List<Registo> historico) {
    }
}
