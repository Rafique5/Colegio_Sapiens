package com.colegiosapiens.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Associa um professor a uma turma que lecciona. */
@Entity
@Table(name = "alocacoes_docentes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"professor_id", "turma_id"}))
public class AlocacaoDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    protected AlocacaoDocente() {
    }

    public AlocacaoDocente(Professor professor, Turma turma) {
        this.professor = professor;
        this.turma = turma;
    }

    public Long getId() { return id; }
    public Professor getProfessor() { return professor; }
    public Turma getTurma() { return turma; }
}
