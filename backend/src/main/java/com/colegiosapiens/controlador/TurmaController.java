package com.colegiosapiens.controlador;

import com.colegiosapiens.dto.AlunoDto;
import com.colegiosapiens.dto.TurmaDto;
import com.colegiosapiens.servico.TurmaServico;
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

/** US28: API de turmas. */
@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

    private final TurmaServico servico;

    public TurmaController(TurmaServico servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TurmaDto.Resposta cadastrar(@Valid @RequestBody TurmaDto.Requisicao dados) {
        return servico.cadastrar(dados);
    }

    @GetMapping
    public List<TurmaDto.Resposta> listar() {
        return servico.listar();
    }

    /** Alunos de uma turma. */
    @GetMapping("/{id}/alunos")
    public List<AlunoDto.Resposta> listarAlunos(@PathVariable Long id) {
        return servico.listarAlunos(id);
    }
}
