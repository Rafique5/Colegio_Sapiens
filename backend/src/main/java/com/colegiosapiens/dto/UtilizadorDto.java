package com.colegiosapiens.dto;

import com.colegiosapiens.modelo.Perfil;
import com.colegiosapiens.modelo.Utilizador;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Objectos de transferência de dados dos utilizadores em geral (US33).
 * Nunca expomos a entidade directamente: a senha nunca sai da API.
 */
public interface UtilizadorDto {

    /** Pedido para criar uma conta de Administrador ou de Secretaria. */
    record CriarConta(
            @NotBlank @Size(max = 150) String nomeCompleto,
            @NotBlank @Email @Size(max = 150) String email,
            @Size(max = 30) String telefone,
            @NotBlank @Size(min = 8, max = 72) String senha,
            @NotNull Perfil perfil,
            /** Cargo (administrador) ou sector (secretaria). Opcional. */
            @Size(max = 100) String cargoOuSetor) {
    }

    /** Auto-registo (ecrã Criar conta): cria um encarregado de educação. */
    record Registo(@NotBlank @Size(max = 150) String nomeCompleto,
                   @NotBlank @Email @Size(max = 150) String email,
                   @NotBlank @Size(min = 8, max = 72) String senha) {
    }

    /** Dados devolvidos ao cliente. */
    record Resposta(Long id, String nomeCompleto, String email, String telefone,
                    Perfil perfil, boolean ativo) {

        public static Resposta de(Utilizador u) {
            return new Resposta(u.getId(), u.getNomeCompleto(), u.getEmail(),
                    u.getTelefone(), u.getPerfil(), u.isAtivo());
        }
    }
}
