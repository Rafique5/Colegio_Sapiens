package com.colegiosapiens.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Utilizador com acesso total à gestão do sistema.
 */
@Entity
@Table(name = "administradores")
public class Administrador extends Utilizador {

    @Column(length = 100)
    private String cargo;

    public Administrador() {
        super(Perfil.ADMINISTRADOR);
    }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
}
