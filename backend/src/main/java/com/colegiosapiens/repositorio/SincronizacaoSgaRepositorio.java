package com.colegiosapiens.repositorio;

import com.colegiosapiens.modelo.SincronizacaoSga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acesso ao histórico de sincronizações. */
public interface SincronizacaoSgaRepositorio extends JpaRepository<SincronizacaoSga, Long> {

    List<SincronizacaoSga> findTop10ByOrderByDataHoraDesc();
}
