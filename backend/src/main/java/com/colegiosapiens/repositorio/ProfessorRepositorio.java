package com.colegiosapiens.repositorio;

import com.colegiosapiens.modelo.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso aos dados dos professores. */
public interface ProfessorRepositorio extends JpaRepository<Professor, Long> {

    boolean existsByCodigoDocente(String codigoDocente);
}
