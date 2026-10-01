package com.colegiosapiens.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Funcionário da secretaria: cadastra alunos e encarregados e faz as associações entre eles.
 */
@Entity
@Table(name = "secretarias")
public class Secretaria extends Utilizador {

    @Column(length = 100)
    private String setor;

    public Secretaria() {
        super(Perfil.SECRETARIA);
    }

    public String getSetor() { return setor; }
    public void setSetor(String setor) { this.setor = setor; }
}
