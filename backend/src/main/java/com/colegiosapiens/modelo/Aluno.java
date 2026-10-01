package com.colegiosapiens.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Aluno do colégio.
 * Herda de Utilizador porque, se o colégio quiser, o aluno poderá ter acesso limitado.
 * Sem senha definida, o aluno existe no sistema mas não consegue iniciar sessão.
 *
 * Nota: nesta sprint o aluno fica ligado directamente à turma. A entidade Matricula
 * (histórico por ano lectivo) poderá ser introduzida numa sprint posterior.
 */
@Entity
@Table(name = "alunos")
public class Aluno extends Utilizador {

    @Column(name = "codigo_estudante", nullable = false, unique = true, length = 30)
    private String codigoEstudante;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turma_id", nullable = false)
    private Turma turma;

    /** ATIVO, INATIVO ou TRANSFERIDO (nulo = ATIVO, para registos antigos). */
    @Column(length = 20)
    private String situacao;

    public Aluno() {
        super(Perfil.ALUNO);
    }

    public String getCodigoEstudante() { return codigoEstudante; }
    public void setCodigoEstudante(String codigoEstudante) { this.codigoEstudante = codigoEstudante; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getSituacao() { return situacao == null ? "ATIVO" : situacao; }
    public void setSituacao(String situacao) { this.situacao = situacao; }

    public Turma getTurma() { return turma; }
    public void setTurma(Turma turma) { this.turma = turma; }
}
