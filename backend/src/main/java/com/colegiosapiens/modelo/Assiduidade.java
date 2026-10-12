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

/** Presença ou falta de um aluno num dia. Um registo por aluno e por dia. */
@Entity
@Table(name = "assiduidade",
        uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "data_aula"}))
public class Assiduidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Column(name = "data_aula", nullable = false)
    private LocalDate dataAula;

    @Column(nullable = false)
    private boolean presente;

    protected Assiduidade() {
    }

    public Assiduidade(Aluno aluno, LocalDate dataAula, boolean presente) {
        this.aluno = aluno;
        this.dataAula = dataAula;
        this.presente = presente;
    }

    public Long getId() { return id; }
    public Aluno getAluno() { return aluno; }
    public LocalDate getDataAula() { return dataAula; }
    public boolean isPresente() { return presente; }
    public void setPresente(boolean presente) { this.presente = presente; }
}
