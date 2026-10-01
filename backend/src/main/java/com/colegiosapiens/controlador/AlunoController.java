package com.colegiosapiens.controlador;

import com.colegiosapiens.dto.AlunoDto;
import com.colegiosapiens.servico.AlunoServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** US27: API de alunos. */
@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoServico servico;

    public AlunoController(AlunoServico servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlunoDto.Resposta cadastrar(@Valid @RequestBody AlunoDto.Requisicao dados) {
        return servico.cadastrar(dados);
    }

    @PutMapping("/{id}/situacao")
    public AlunoDto.Resposta alterarSituacao(@PathVariable Long id, @Valid @RequestBody AlunoDto.Situacao dados) {
        return servico.alterarSituacao(id, dados.situacao());
    }

    @GetMapping
    public List<AlunoDto.Resposta> listar() {
        return servico.listar();
    }
}
