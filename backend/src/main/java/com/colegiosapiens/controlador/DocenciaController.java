package com.colegiosapiens.controlador;

import com.colegiosapiens.dto.AlunoDto;
import com.colegiosapiens.dto.DocenciaDto;
import com.colegiosapiens.servico.DocenciaServico;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Associação de professores a turmas (administrador) e área do professor (/api/professor/**). */
@RestController
@RequestMapping("/api")
public class DocenciaController {

    private final DocenciaServico servico;

    public DocenciaController(DocenciaServico servico) {
        this.servico = servico;
    }

    @GetMapping("/alocacoes")
    public List<DocenciaDto.AlocacaoResposta> listarAlocacoes() {
        return servico.listarAlocacoes();
    }

    @PostMapping("/alocacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public DocenciaDto.AlocacaoResposta alocar(@Valid @RequestBody DocenciaDto.AlocacaoRequisicao dados) {
        return servico.alocar(dados);
    }

    @DeleteMapping("/alocacoes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        servico.removerAlocacao(id);
    }

    @GetMapping("/professor/turmas")
    public List<DocenciaDto.TurmaSimples> turmas(Authentication autenticacao) {
        return servico.turmasDoProfessor(Long.valueOf(autenticacao.getName()));
    }

    @GetMapping("/professor/alunos")
    public List<AlunoDto.Resposta> alunos(Authentication autenticacao) {
        return servico.alunosDoProfessor(Long.valueOf(autenticacao.getName()));
    }

    @GetMapping("/professor/assiduidade")
    public List<DocenciaDto.AlunoAssiduidade> assiduidade(
            Authentication autenticacao, @RequestParam Long turmaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return servico.assiduidade(Long.valueOf(autenticacao.getName()), turmaId, data);
    }

    @PostMapping("/professor/assiduidade")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> registarAssiduidade(Authentication autenticacao,
                                                   @Valid @RequestBody DocenciaDto.AssiduidadeRequisicao dados) {
        int n = servico.registarAssiduidade(Long.valueOf(autenticacao.getName()), dados);
        return Map.of("mensagem", "Assiduidade guardada (" + n + " registos).");
    }
}
