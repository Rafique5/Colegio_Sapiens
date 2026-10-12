package com.colegiosapiens.dto;

/** Contas de acesso (visão da secretaria). */
public final class ContaDto {

    private ContaDto() {
    }

    /** estado: ATIVA, PENDENTE (sem senha definida) ou INATIVA. */
    public record Resposta(Long id, String nomeCompleto, String email, String telefone,
                           String perfil, String codigo, String estado) {
    }

    /** Credencial gerada; a senha temporária só é mostrada uma vez. */
    public record Credencial(String nome, String identificador, String senhaTemporaria) {
    }
}
