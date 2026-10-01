package com.colegiosapiens.controlador;

import com.colegiosapiens.dto.VinculoDto;
import com.colegiosapiens.servico.VinculoServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** US11: API de associação entre alunos e encarregados. */
@RestController
@RequestMapping("/api/vinculos")
public class VinculoController {

    private final VinculoServico servico;

    public VinculoController(VinculoServico servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VinculoDto.Resposta associar(@Valid @RequestBody VinculoDto.Requisicao dados) {
        return servico.associar(dados);
    }

    @GetMapping
    public List<VinculoDto.Resposta> listar() {
        return servico.listarTodos();
    }

    /** Educandos de um encarregado. */
    @GetMapping("/encarregado/{id}")
    public List<VinculoDto.Resposta> porEncarregado(@PathVariable Long id) {
        return servico.listarPorEncarregado(id);
    }

    /** Encarregados de um aluno. */
    @GetMapping("/aluno/{id}")
    public List<VinculoDto.Resposta> porAluno(@PathVariable Long id) {
        return servico.listarPorAluno(id);
    }
}
