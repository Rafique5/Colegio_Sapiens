package com.colegiosapiens.dto;

import com.colegiosapiens.modelo.Aluno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Objectos de transferência de dados dos alunos (US27).
 * E-mail, telefone e senha são opcionais: só são necessários se o colégio
 * quiser dar acesso à plataforma ao aluno.
 */
public interface AlunoDto {

    record Requisicao(
            @NotBlank @Size(max = 150) String nomeCompleto,
            @NotBlank @Size(max = 30) String codigoEstudante,
            @NotNull @Past LocalDate dataNascimento,
            @NotNull Long turmaId,
            @Email @Size(max = 150) String email,
            @Size(max = 30) String telefone,
            @Pattern(regexp = "^$|^.{8,72}$", message = "deve ter entre 8 e 72 caracteres") String senha) {
    }

    record Situacao(@jakarta.validation.constraints.NotBlank
                    @Pattern(regexp = "ATIVO|INATIVO|TRANSFERIDO", message = "deve ser ATIVO, INATIVO ou TRANSFERIDO")
                    String situacao) {
    }

    record Resposta(Long id, String nomeCompleto, String codigoEstudante,
                    LocalDate dataNascimento, Long turmaId, String nomeTurma, boolean ativo,
                    String situacao) {

        public static Resposta de(Aluno a) {
            return new Resposta(a.getId(), a.getNomeCompleto(), a.getCodigoEstudante(),
                    a.getDataNascimento(), a.getTurma().getId(), a.getTurma().getNomeTurma(),
                    a.isAtivo(), a.getSituacao());
        }
    }
}
