package com.colegiosapiens.controlador;

import com.colegiosapiens.dto.UtilizadorDto;
import com.colegiosapiens.servico.UtilizadorServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** US33: gestão de contas de acesso (apenas administrador). */
@RestController
@RequestMapping("/api/utilizadores")
public class UtilizadorController {

    private final UtilizadorServico servico;

    public UtilizadorController(UtilizadorServico servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UtilizadorDto.Resposta criarConta(@Valid @RequestBody UtilizadorDto.CriarConta dados) {
        return servico.criarConta(dados);
    }

    @GetMapping
    public List<UtilizadorDto.Resposta> listar() {
        return servico.listar();
    }
}
