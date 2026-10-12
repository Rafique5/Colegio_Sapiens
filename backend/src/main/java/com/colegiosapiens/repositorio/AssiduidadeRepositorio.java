package com.colegiosapiens.repositorio;

import com.colegiosapiens.modelo.Assiduidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Acesso aos registos de assiduidade. */
public interface AssiduidadeRepositorio extends JpaRepository<Assiduidade, Long> {

    List<Assiduidade> findByAlunoTurmaIdAndDataAula(Long turmaId, LocalDate dataAula);

    Optional<Assiduidade> findByAlunoIdAndDataAula(Long alunoId, LocalDate dataAula);
}
