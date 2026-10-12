package com.colegiosapiens.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Encarregado de educação: recebe as comunicações do colégio e acompanha os seus educandos.
 * A ligação aos alunos é feita através de VinculoParental.
 */
@Entity
@Table(name = "encarregados")
public class EncarregadoEducacao extends Utilizador {

    /** Será usado na US23 (notificação de boas-vindas após o registo). */
    @Column(name = "notificacao_boas_vindas_enviada", nullable = false)
    private boolean notificacaoBoasVindasEnviada = false;

    @Column(length = 100)
    private String profissao;

    public EncarregadoEducacao() {
        super(Perfil.ENCARREGADO);
    }

    public String getProfissao() { return profissao; }
    public void setProfissao(String profissao) { this.profissao = profissao; }

    public boolean isNotificacaoBoasVindasEnviada() { return notificacaoBoasVindasEnviada; }
    public void setNotificacaoBoasVindasEnviada(boolean valor) { this.notificacaoBoasVindasEnviada = valor; }
}
