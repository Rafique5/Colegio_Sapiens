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

import java.time.LocalDateTime;

/** Nota de uma avaliação (ex.: Teste 1 de Matemática, 1.º trimestre). Uma por aluno/disciplina/avaliação/trimestre. */
@Entity
@Table(name = "notas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"aluno_id", "disciplina", "avaliacao", "trimestre"}))
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Column(nullable = false, length = 80)
    private String disciplina;

    @Column(nullable = false, length = 30)
    private String avaliacao;

    @Column(nullable = false)
    private int trimestre;

    @Column(nullable = false)
    private double valor;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDateTime dataLancamento = LocalDateTime.now();

    protected Nota() {
    }

    public Nota(Aluno aluno, String disciplina, String avaliacao, int trimestre, double valor) {
        this.aluno = aluno;
        this.disciplina = disciplina;
        this.avaliacao = avaliacao;
        this.trimestre = trimestre;
        this.valor = valor;
    }

    public Long getId() { return id; }
    public Aluno getAluno() { return aluno; }
    public String getDisciplina() { return disciplina; }
    public String getAvaliacao() { return avaliacao; }
    public int getTrimestre() { return trimestre; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; this.dataLancamento = LocalDateTime.now(); }
    public LocalDateTime getDataLancamento() { return dataLancamento; }
}
