package com.colegiosapiens.config;

import com.colegiosapiens.modelo.Utilizador;
import com.colegiosapiens.repositorio.UtilizadorRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * FERRAMENTA SÓ PARA DESENVOLVIMENTO. APAGAR ESTE FICHEIRO antes de entregar ao cliente.
 *
 * Só é carregada no perfil "dev" (nunca em produção).
 *
 * As senhas são guardadas codificadas (BCrypt) e não podem ser lidas. Esta ferramenta faz duas coisas ao arrancar:
 *  1. Lista no log todos os utilizadores (nome, e-mail, telefone, perfil, estado).
 *  2. Define senhas conhecidas, se pedir no application.properties:
 *       sapiens.dev.senha-geral=Teste@1234                      -> todos os utilizadores ficam com esta senha
 *       sapiens.dev.repor=ana@x.com=Nova@1234;jose@x.com=Abc@12345  -> só estes (e-mail ou telefone = nova senha)
 */
@Component
@Profile("dev")
public class FerramentaDev implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(FerramentaDev.class);

    private final UtilizadorRepositorio repositorio;
    private final PasswordEncoder codificador;
    private final String senhaGeral;
    private final String repor;

    public FerramentaDev(UtilizadorRepositorio repositorio, PasswordEncoder codificador,
                         @Value("${sapiens.dev.senha-geral:}") String senhaGeral,
                         @Value("${sapiens.dev.repor:}") String repor) {
        this.repositorio = repositorio;
        this.codificador = codificador;
        this.senhaGeral = senhaGeral;
        this.repor = repor;
    }

    @Override
    public void run(String... args) {
        if (!senhaGeral.isBlank()) {
            for (Utilizador u : repositorio.findAll()) {
                u.setSenhaHash(codificador.encode(senhaGeral));
                repositorio.save(u);
            }
            LOG.warn("[DEV] Todos os utilizadores ficaram com a senha: {}", senhaGeral);
        }
        for (String par : repor.split(";")) {
            int i = par.indexOf('=');
            if (i < 1) {
                continue;
            }
            String id = par.substring(0, i).trim().toLowerCase();
            String nova = par.substring(i + 1).trim();
            Utilizador u = repositorio.findByEmail(id).or(() -> repositorio.findByTelefone(id)).orElse(null);
            if (u == null || nova.isEmpty()) {
                LOG.warn("[DEV] Não encontrei o utilizador '{}'", id);
                continue;
            }
            u.setSenhaHash(codificador.encode(nova));
            repositorio.save(u);
            LOG.warn("[DEV] {} ficou com a senha: {}", id, nova);
        }

        List<Utilizador> todos = repositorio.findAll();
        LOG.warn("[DEV] ===== UTILIZADORES ({}) =====", todos.size());
        for (Utilizador u : todos) {
            LOG.warn("[DEV] #{} | {} | {} | {} | tel: {} | {}", u.getId(), u.getPerfil(), u.getNomeCompleto(),
                    u.getEmail(), u.getTelefone(), u.isAtivo() ? "ativo" : "INATIVO");
        }
    }
}