package com.colegiosapiens.controlador;

import com.colegiosapiens.dto.ContaDto;
import com.colegiosapiens.servico.ContaServico;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Contas de acesso (/api/contas). */
@RestController
@RequestMapping("/api/contas")
public class ContaController {

    private final ContaServico servico;

    public ContaController(ContaServico servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<ContaDto.Resposta> listar() {
        return servico.listar();
    }

    @PostMapping("/gerar-pendentes")
    public List<ContaDto.Credencial> gerarPendentes() {
        return servico.gerarPendentes();
    }

    @PutMapping("/{id}/estado")
    public ContaDto.Resposta definirEstado(@PathVariable Long id, @RequestParam boolean ativa,
                                           Authentication autenticacao) {
        return servico.definirAtiva(id, ativa, Long.valueOf(autenticacao.getName()));
    }
}
