package com.colegiosapiens.repositorio;

import com.colegiosapiens.modelo.EncarregadoEducacao;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso aos dados dos encarregados de educação. */
public interface EncarregadoRepositorio extends JpaRepository<EncarregadoEducacao, Long> {
}
