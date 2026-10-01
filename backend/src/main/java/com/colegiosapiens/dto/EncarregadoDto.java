package com.colegiosapiens.dto;

import com.colegiosapiens.modelo.EncarregadoEducacao;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Objectos de transferência de dados dos encarregados de educação (US26).
 * O telefone é obrigatório (é o contacto principal e serve para iniciar sessão);
 * o e-mail é opcional, porque nem todos os encarregados o possuem.
 */
public interface EncarregadoDto {

    record Requisicao(
            @NotBlank @Size(max = 150) String nomeCompleto,
            @Size(max = 30) String telefone,
            @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8, max = 72) String senha) {
    }

    record Resposta(Long id, String nomeCompleto, String telefone, String email, boolean ativo) {

        public static Resposta de(EncarregadoEducacao e) {
            return new Resposta(e.getId(), e.getNomeCompleto(), e.getTelefone(),
                    e.getEmail(), e.isAtivo());
        }
    }
}
