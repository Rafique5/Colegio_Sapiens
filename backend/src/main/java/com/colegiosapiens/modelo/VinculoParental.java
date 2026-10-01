package com.colegiosapiens.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

/**
 * Liga um aluno ao seu encarregado de educação.
 * Um encarregado pode ter vários educandos e um aluno pode ter mais de um encarregado.
 * É através deste vínculo que cada encarregado só acede aos dados dos seus educandos.
 */
@Entity
@Table(name = "vinculos_parentais",
        uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "encarregado_id"}))
public class VinculoParental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encarregado_id", nullable = false)
    private EncarregadoEducacao encarregado;

    @Column(name = "data_associacao", nullable = false)
    private LocalDate dataAssociacao = LocalDate.now();

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(length = 30)
    private String parentesco;

    public VinculoParental() {
    }

    public VinculoParental(Aluno aluno, EncarregadoEducacao encarregado) {
        this.aluno = aluno;
        this.encarregado = encarregado;
    }

    public Long getId() { return id; }

    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }

    public EncarregadoEducacao getEncarregado() { return encarregado; }
    public void setEncarregado(EncarregadoEducacao encarregado) { this.encarregado = encarregado; }

    public LocalDate getDataAssociacao() { return dataAssociacao; }
    public void setDataAssociacao(LocalDate dataAssociacao) { this.dataAssociacao = dataAssociacao; }

    public String getParentesco() { return parentesco; }
    public void setParentesco(String parentesco) { this.parentesco = parentesco; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
