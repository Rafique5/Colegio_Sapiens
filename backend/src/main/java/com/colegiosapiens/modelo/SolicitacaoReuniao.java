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

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Pedido de reunião de um professor a um encarregado, a propósito de um aluno. Estados: ENVIADA, LIDA. */
@Entity
@Table(name = "solicitacoes_reuniao")
public class SolicitacaoReuniao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encarregado_id", nullable = false)
    private EncarregadoEducacao encarregado;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Column(name = "data_sugerida", nullable = false)
    private LocalDate dataSugerida;

    @Column(nullable = false, length = 20)
    private String estado = "ENVIADA";

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm = LocalDateTime.now();

    @Column(name = "lida_em")
    private LocalDateTime lidaEm;

    protected SolicitacaoReuniao() {
    }

    public SolicitacaoReuniao(Professor professor, Aluno aluno, EncarregadoEducacao encarregado,
                              String motivo, LocalDate dataSugerida) {
        this.professor = professor;
        this.aluno = aluno;
        this.encarregado = encarregado;
        this.motivo = motivo;
        this.dataSugerida = dataSugerida;
    }

    /** Regista a visualização (só na primeira vez). */
    public void marcarComoLida() {
        if ("ENVIADA".equals(estado)) {
            estado = "LIDA";
            lidaEm = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public Professor getProfessor() { return professor; }
    public Aluno getAluno() { return aluno; }
    public EncarregadoEducacao getEncarregado() { return encarregado; }
    public String getMotivo() { return motivo; }
    public LocalDate getDataSugerida() { return dataSugerida; }
    public String getEstado() { return estado; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public LocalDateTime getLidaEm() { return lidaEm; }
}
