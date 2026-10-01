package com.colegiosapiens.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Turma do colégio (ex.: "10ª Classe - Turma A" no ano lectivo 2026).
 * Não pode haver duas turmas com o mesmo nome no mesmo ano lectivo.
 */
@Entity
@Table(name = "turmas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"nome_turma", "ano_lectivo"}))
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_turma", nullable = false, length = 50)
    private String nomeTurma;

    @Column(name = "ano_lectivo", nullable = false)
    private int anoLectivo;

    @Column(length = 50)
    private String sala;

    public Turma() {
    }

    public Long getId() { return id; }

    public String getNomeTurma() { return nomeTurma; }
    public void setNomeTurma(String nomeTurma) { this.nomeTurma = nomeTurma; }

    public int getAnoLectivo() { return anoLectivo; }
    public void setAnoLectivo(int anoLectivo) { this.anoLectivo = anoLectivo; }

    public String getSala() { return sala; }
    public void setSala(String sala) { this.sala = sala; }
}
