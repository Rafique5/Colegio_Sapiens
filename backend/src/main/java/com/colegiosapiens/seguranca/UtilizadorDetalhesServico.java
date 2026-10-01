package com.colegiosapiens.seguranca;

import com.colegiosapiens.modelo.Utilizador;
import com.colegiosapiens.repositorio.UtilizadorRepositorio;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Diz ao Spring Security como encontrar um utilizador no momento do login.
 * O identificador pode ser o e-mail (contém "@") ou o telefone.
 * O "username" interno da sessão passa a ser o id do utilizador.
 */
@Service
public class UtilizadorDetalhesServico implements UserDetailsService {

    private final UtilizadorRepositorio repositorio;

    public UtilizadorDetalhesServico(UtilizadorRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identificador) throws UsernameNotFoundException {
        String valor = identificador == null ? "" : identificador.trim();

        Optional<Utilizador> encontrado = valor.contains("@")
                ? repositorio.findByEmail(valor.toLowerCase())
                : repositorio.findByTelefone(valor.replaceAll("\\s+", ""));

        Utilizador utilizador = encontrado
                .filter(u -> u.getSenhaHash() != null)   // sem senha = sem acesso
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado."));

        return User.withUsername(String.valueOf(utilizador.getId()))
                .password(utilizador.getSenhaHash())
                .roles(utilizador.getPerfil().name())     // ROLE_ADMINISTRADOR, ROLE_PROFESSOR, ...
                .disabled(!utilizador.isAtivo())
                .build();
    }
}
