package com.colegiosapiens.repositorio;

import com.colegiosapiens.modelo.Nota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Acesso às notas dos alunos. */
public interface NotaRepositorio extends JpaRepository<Nota, Long> {

    List<Nota> findByAlunoIdAndTrimestre(Long alunoId, int trimestre);

    Optional<Nota> findByAlunoIdAndDisciplinaIgnoreCaseAndAvaliacaoAndTrimestre(
            Long alunoId, String disciplina, String avaliacao, int trimestre);
}
