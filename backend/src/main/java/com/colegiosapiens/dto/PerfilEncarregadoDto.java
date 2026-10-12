package com.colegiosapiens.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Perfil completo do encarregado (contactos e vínculos com os educandos). */
public final class PerfilEncarregadoDto {

    private PerfilEncarregadoDto() {
    }

    public record Educando(Long vinculoId, Long alunoId, String nomeAluno, String turma,
                           String parentesco, boolean validado) {
    }

    public record Resposta(Long id, String nomeCompleto, String telefone, String email,
                           String profissao, List<Educando> educandos) {
    }

    public record Validacao(@NotNull Long vinculoId, boolean validado) {
    }

    public record Requisicao(@Size(max = 30) String telefone,
                             @Email @Size(max = 150) String email,
                             @Size(max = 100) String profissao,
                             @Valid List<Validacao> vinculos) {
    }
}
