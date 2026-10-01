package com.colegiosapiens.controlador;

import com.colegiosapiens.dto.EncarregadoDto;
import com.colegiosapiens.servico.EncarregadoServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** US26: API de encarregados de educação. */
@RestController
@RequestMapping("/api/encarregados")
public class EncarregadoController {

    private final EncarregadoServico servico;

    public EncarregadoController(EncarregadoServico servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EncarregadoDto.Resposta cadastrar(@Valid @RequestBody EncarregadoDto.Requisicao dados) {
        return servico.cadastrar(dados);
    }

    @GetMapping
    public List<EncarregadoDto.Resposta> listar() {
        return servico.listar();
    }
}
