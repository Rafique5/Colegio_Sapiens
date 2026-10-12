package com.colegiosapiens.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Histórico das sincronizações com o SGA. Estado: CONCLUIDA ou FALHOU. */
@Entity
@Table(name = "sincronizacoes_sga")
public class SincronizacaoSga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private int registos;

    @Column(nullable = false)
    private int novos;

    @Column(nullable = false)
    private int alterados;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(length = 255)
    private String mensagem;

    protected SincronizacaoSga() {
    }

    public SincronizacaoSga(LocalDateTime dataHora, int registos, int novos, int alterados,
                            String estado, String mensagem) {
        this.dataHora = dataHora;
        this.registos = registos;
        this.novos = novos;
        this.alterados = alterados;
        this.estado = estado;
        this.mensagem = mensagem == null ? null : (mensagem.length() > 250 ? mensagem.substring(0, 250) : mensagem);
    }

    public Long getId() { return id; }
    public LocalDateTime getDataHora() { return dataHora; }
    public int getRegistos() { return registos; }
    public int getNovos() { return novos; }
    public int getAlterados() { return alterados; }
    public String getEstado() { return estado; }
    public String getMensagem() { return mensagem; }
}
