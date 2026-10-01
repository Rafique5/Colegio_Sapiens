package com.colegiosapiens.controlador;

import com.colegiosapiens.dto.EncarregadoDto;
import com.colegiosapiens.dto.UtilizadorDto;
import com.colegiosapiens.servico.EncarregadoServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.colegiosapiens.servico.UtilizadorServico;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * O login (POST /api/auth/login) e o logout (POST /api/auth/logout) são tratados
 * pelo Spring Security. Aqui fica apenas a consulta do utilizador com sessão iniciada.
 */
@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final UtilizadorServico servico;
    private final EncarregadoServico encarregadoServico;

    public AutenticacaoController(UtilizadorServico servico, EncarregadoServico encarregadoServico) {
        this.servico = servico;
        this.encarregadoServico = encarregadoServico;
    }

    /** Auto-registo público: cria um encarregado (fica sem educandos até a secretaria os associar). */
    @PostMapping("/registo")
    @ResponseStatus(HttpStatus.CREATED)
    public EncarregadoDto.Resposta registar(@Valid @RequestBody UtilizadorDto.Registo dados) {
        return encarregadoServico.cadastrar(
                new EncarregadoDto.Requisicao(dados.nomeCompleto(), null, dados.email(), dados.senha()));
    }

    /** Devolve os dados do utilizador autenticado (o nome da sessão é o id). */
    @GetMapping("/eu")
    public UtilizadorDto.Resposta utilizadorActual(Authentication autenticacao) {
        return servico.obterPorId(Long.valueOf(autenticacao.getName()));
    }
}
