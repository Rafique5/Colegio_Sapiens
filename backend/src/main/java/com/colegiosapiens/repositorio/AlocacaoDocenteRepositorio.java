package com.colegiosapiens.repositorio;

import com.colegiosapiens.modelo.AlocacaoDocente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acesso às turmas de cada professor. */
public interface AlocacaoDocenteRepositorio extends JpaRepository<AlocacaoDocente, Long> {

    List<AlocacaoDocente> findByProfessorId(Long professorId);

    boolean existsByProfessorIdAndTurmaId(Long professorId, Long turmaId);
}
