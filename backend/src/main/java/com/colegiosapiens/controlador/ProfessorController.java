package com.colegiosapiens.controlador;

import com.colegiosapiens.dto.ProfessorDto;
import com.colegiosapiens.servico.ProfessorServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** US01: API de professores. */
@RestController
@RequestMapping("/api/professores")
public class ProfessorController {

    private final ProfessorServico servico;

    public ProfessorController(ProfessorServico servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfessorDto.Resposta cadastrar(@Valid @RequestBody ProfessorDto.Requisicao dados) {
        return servico.cadastrar(dados);
    }

    @GetMapping
    public List<ProfessorDto.Resposta> listar() {
        return servico.listar();
    }
}
