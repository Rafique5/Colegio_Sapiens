package com.colegiosapiens.repositorio;

import com.colegiosapiens.modelo.SolicitacaoReuniao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acesso aos pedidos de reunião. */
public interface SolicitacaoReuniaoRepositorio extends JpaRepository<SolicitacaoReuniao, Long> {

    List<SolicitacaoReuniao> findByEncarregadoIdOrderByCriadaEmDesc(Long encarregadoId);
}
