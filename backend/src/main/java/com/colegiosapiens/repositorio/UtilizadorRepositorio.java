package com.colegiosapiens.repositorio;

import com.colegiosapiens.modelo.Perfil;
import com.colegiosapiens.modelo.Utilizador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acesso aos dados de qualquer utilizador (administrador, professor, encarregado, etc.).
 */
public interface UtilizadorRepositorio extends JpaRepository<Utilizador, Long> {

    Optional<Utilizador> findByEmail(String email);

    Optional<Utilizador> findByTelefone(String telefone);

    boolean existsByEmail(String email);

    boolean existsByTelefone(String telefone);

    boolean existsByPerfil(Perfil perfil);
}
