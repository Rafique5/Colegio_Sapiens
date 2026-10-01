package com.colegiosapiens.repositorio;

import com.colegiosapiens.modelo.VinculoParental;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acesso aos vínculos entre alunos e encarregados. */
public interface VinculoParentalRepositorio extends JpaRepository<VinculoParental, Long> {

    List<VinculoParental> findByAtivoTrue();

    boolean existsByAlunoIdAndEncarregadoId(Long alunoId, Long encarregadoId);

    List<VinculoParental> findByEncarregadoIdAndAtivoTrue(Long encarregadoId);

    List<VinculoParental> findByAlunoIdAndAtivoTrue(Long alunoId);
}
