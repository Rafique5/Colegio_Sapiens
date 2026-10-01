package com.colegiosapiens.dto;

import com.colegiosapiens.modelo.Professor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Objectos de transferência de dados dos professores (US01). */
public interface ProfessorDto {

    record Requisicao(
            @NotBlank @Size(max = 150) String nomeCompleto,
            @NotBlank @Email @Size(max = 150) String email,
            @Size(max = 30) String telefone,
            @NotBlank @Size(min = 8, max = 72) String senha,
            @NotBlank @Size(max = 30) String codigoDocente,
            @Size(max = 100) String especialidade) {
    }

    record Resposta(Long id, String nomeCompleto, String email, String telefone,
                    String codigoDocente, String especialidade, boolean ativo) {

        public static Resposta de(Professor p) {
            return new Resposta(p.getId(), p.getNomeCompleto(), p.getEmail(), p.getTelefone(),
                    p.getCodigoDocente(), p.getEspecialidade(), p.isAtivo());
        }
    }
}
