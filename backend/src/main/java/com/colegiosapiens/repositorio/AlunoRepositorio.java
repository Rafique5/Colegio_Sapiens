package com.colegiosapiens.repositorio;

import com.colegiosapiens.modelo.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acesso aos dados dos alunos. */
public interface AlunoRepositorio extends JpaRepository<Aluno, Long> {

    boolean existsByCodigoEstudante(String codigoEstudante);

    /** Lista os alunos de uma turma (navega pela propriedade turma.id). */
    List<Aluno> findByTurmaId(Long turmaId);
}
