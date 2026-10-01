package com.colegiosapiens.servico;

import com.colegiosapiens.dto.UtilizadorDto;
import com.colegiosapiens.excepcao.RecursoNaoEncontradoException;
import com.colegiosapiens.excepcao.RegraNegocioException;
import com.colegiosapiens.modelo.Administrador;
import com.colegiosapiens.modelo.Perfil;
import com.colegiosapiens.modelo.Secretaria;
import com.colegiosapiens.modelo.Utilizador;
import com.colegiosapiens.repositorio.UtilizadorRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras comuns a todos os utilizadores e criação de contas de acesso (US33).
 * Os serviços de professor, encarregado e aluno reutilizam o método
 * preencherContaBase para não repetir validações.
 */
@Service
public class UtilizadorServico {

    private final UtilizadorRepositorio repositorio;
    private final PasswordEncoder codificador;

    public UtilizadorServico(UtilizadorRepositorio repositorio, PasswordEncoder codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }

    /**
     * US33: o administrador cria contas de acesso.
     * Aqui só se criam Administradores e Secretarias; professores, encarregados e
     * alunos recebem a conta no seu próprio cadastro.
     */
    @Transactional
    public UtilizadorDto.Resposta criarConta(UtilizadorDto.CriarConta dados) {
        if (dados.perfil() != Perfil.ADMINISTRADOR && dados.perfil() != Perfil.SECRETARIA) {
            throw new RegraNegocioException(
                    "Por este meio só se criam contas de ADMINISTRADOR ou SECRETARIA. "
                            + "Professores, encarregados e alunos são criados no respectivo cadastro.");
        }

        Utilizador novo;
        if (dados.perfil() == Perfil.ADMINISTRADOR) {
            Administrador administrador = new Administrador();
            administrador.setCargo(dados.cargoOuSetor());
            novo = administrador;
        } else {
            Secretaria secretaria = new Secretaria();
            secretaria.setSetor(dados.cargoOuSetor());
            novo = secretaria;
        }

        preencherContaBase(novo, dados.nomeCompleto(), dados.email(), dados.telefone(), dados.senha());
        return UtilizadorDto.Resposta.de(repositorio.save(novo));
    }

    @Transactional(readOnly = true)
    public List<UtilizadorDto.Resposta> listar() {
        return repositorio.findAll().stream().map(UtilizadorDto.Resposta::de).toList();
    }

    @Transactional(readOnly = true)
    public UtilizadorDto.Resposta obterPorId(Long id) {
        return repositorio.findById(id)
                .map(UtilizadorDto.Resposta::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Utilizador não encontrado."));
    }

    /**
     * Preenche os dados comuns de um utilizador novo:
     * normaliza contactos, garante que e-mail e telefone não estão em uso
     * e guarda a senha já codificada (nunca em texto simples).
     */
    public void preencherContaBase(Utilizador utilizador, String nomeCompleto,
                                   String email, String telefone, String senha) {
        String emailNormalizado = normalizarEmail(email);
        String telefoneNormalizado = normalizarTelefone(telefone);

        if (emailNormalizado != null && repositorio.existsByEmail(emailNormalizado)) {
            throw new RegraNegocioException("Já existe um utilizador com este e-mail.");
        }
        if (telefoneNormalizado != null && repositorio.existsByTelefone(telefoneNormalizado)) {
            throw new RegraNegocioException("Já existe um utilizador com este telefone.");
        }
        if (senha != null && emailNormalizado == null && telefoneNormalizado == null) {
            throw new RegraNegocioException(
                    "Para definir uma senha é necessário indicar o e-mail ou o telefone.");
        }

        utilizador.setNomeCompleto(nomeCompleto.trim());
        utilizador.setEmail(emailNormalizado);
        utilizador.setTelefone(telefoneNormalizado);
        if (senha != null) {
            utilizador.setSenhaHash(codificador.encode(senha));
        }
    }

    /** E-mail em minúsculas e sem espaços; texto vazio passa a nulo. */
    public static String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    /** Telefone sem espaços; texto vazio passa a nulo. */
    public static String normalizarTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) {
            return null;
        }
        return telefone.replaceAll("\\s+", "");
    }
}
