package com.colegiosapiens.repositorio;

import com.colegiosapiens.modelo.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acesso aos dados das turmas. */
public interface TurmaRepositorio extends JpaRepository<Turma, Long> {

    boolean existsByNomeTurmaAndAnoLectivo(String nomeTurma, int anoLectivo);

    java.util.Optional<com.colegiosapiens.modelo.Turma> findByNomeTurmaAndAnoLectivo(String nomeTurma, int anoLectivo);
}
