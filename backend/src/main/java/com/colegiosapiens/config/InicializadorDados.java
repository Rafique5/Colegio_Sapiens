package com.colegiosapiens.config;

import com.colegiosapiens.modelo.Administrador;
import com.colegiosapiens.modelo.Perfil;
import com.colegiosapiens.repositorio.UtilizadorRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria o administrador inicial no primeiro arranque, para ser possível
 * entrar no sistema e criar as restantes contas. Os dados vêm do application.properties.
 */
@Component
public class InicializadorDados implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InicializadorDados.class);

    private final UtilizadorRepositorio repositorio;
    private final PasswordEncoder codificador;

    @Value("${sapiens.admin.nome}")
    private String nome;

    @Value("${sapiens.admin.email}")
    private String email;

    @Value("${sapiens.admin.senha}")
    private String senha;

    public InicializadorDados(UtilizadorRepositorio repositorio, PasswordEncoder codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }

    @Override
    public void run(String... args) {
        // Só cria se ainda não existir nenhum administrador
        if (repositorio.existsByPerfil(Perfil.ADMINISTRADOR)) {
            return;
        }

        Administrador administrador = new Administrador();
        administrador.setNomeCompleto(nome);
        administrador.setEmail(email.trim().toLowerCase());
        administrador.setSenhaHash(codificador.encode(senha));
        administrador.setCargo("Administrador do sistema");
        repositorio.save(administrador);

        LOG.info("Administrador inicial criado: {}. Altere a senha após o primeiro acesso.", email);
    }
}
