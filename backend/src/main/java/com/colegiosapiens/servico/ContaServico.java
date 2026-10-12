package com.colegiosapiens.servico;

import com.colegiosapiens.dto.ContaDto;
import com.colegiosapiens.excepcao.RecursoNaoEncontradoException;
import com.colegiosapiens.excepcao.RegraNegocioException;
import com.colegiosapiens.modelo.Aluno;
import com.colegiosapiens.modelo.Professor;
import com.colegiosapiens.modelo.Utilizador;
import com.colegiosapiens.repositorio.UtilizadorRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/** Contas de acesso: consulta, geração das contas pendentes e (des)activação. */
@Service
public class ContaServico {

    private static final String ALFABETO = "abcdefghjkmnpqrstuvwxyz23456789";

    private final UtilizadorRepositorio repositorio;
    private final PasswordEncoder codificador;
    private final SecureRandom aleatorio = new SecureRandom();

    public ContaServico(UtilizadorRepositorio repositorio, PasswordEncoder codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }

    @Transactional(readOnly = true)
    public List<ContaDto.Resposta> listar() {
        return repositorio.findAll().stream().map(this::paraResposta).toList();
    }

    /** Dá uma senha temporária a cada conta activa que ainda não tem senha. */
    @Transactional
    public List<ContaDto.Credencial> gerarPendentes() {
        List<ContaDto.Credencial> geradas = new ArrayList<>();
        for (Utilizador u : repositorio.findAll()) {
            if (!u.isAtivo() || u.getSenhaHash() != null) {
                continue;
            }
            String identificador = u.getEmail() != null ? u.getEmail() : u.getTelefone();
            if (identificador == null) {
                continue;
            }
            String senha = gerarSenha();
            u.setSenhaHash(codificador.encode(senha));
            geradas.add(new ContaDto.Credencial(u.getNomeCompleto(), identificador, senha));
        }
        return geradas;
    }

    /** Activa ou desactiva uma conta (sem apagar o histórico). Ninguém desactiva a própria conta. */
    @Transactional
    public ContaDto.Resposta definirAtiva(Long id, boolean ativa, Long idActual) {
        Utilizador u = repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada."));
        if (!ativa && u.getId().equals(idActual)) {
            throw new RegraNegocioException("Não pode desactivar a sua própria conta.");
        }
        u.setAtivo(ativa);
        return paraResposta(u);
    }

    private ContaDto.Resposta paraResposta(Utilizador u) {
        String codigo = u instanceof Aluno a ? a.getCodigoEstudante()
                : (u instanceof Professor p ? p.getCodigoDocente() : null);
        String estado = !u.isAtivo() ? "INATIVA" : (u.getSenhaHash() == null ? "PENDENTE" : "ATIVA");
        return new ContaDto.Resposta(u.getId(), u.getNomeCompleto(), u.getEmail(), u.getTelefone(),
                u.getPerfil().name(), codigo, estado);
    }

    private String gerarSenha() {
        StringBuilder s = new StringBuilder("Sp@");
        for (int i = 0; i < 6; i++) {
            s.append(ALFABETO.charAt(aleatorio.nextInt(ALFABETO.length())));
        }
        return s.append('9').toString();
    }
}
