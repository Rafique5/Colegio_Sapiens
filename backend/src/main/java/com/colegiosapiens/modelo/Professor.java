package com.colegiosapiens.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Docente do colégio.
 * Nas próximas sprints será associado a disciplinas e turmas (AlocacaoDocente).
 */
@Entity
@Table(name = "professores")
public class Professor extends Utilizador {

    @Column(name = "codigo_docente", nullable = false, unique = true, length = 30)
    private String codigoDocente;

    @Column(length = 100)
    private String especialidade;

    public Professor() {
        super(Perfil.PROFESSOR);
    }

    public String getCodigoDocente() { return codigoDocente; }
    public void setCodigoDocente(String codigoDocente) { this.codigoDocente = codigoDocente; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
}
