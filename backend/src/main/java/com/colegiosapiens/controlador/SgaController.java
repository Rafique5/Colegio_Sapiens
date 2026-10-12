package com.colegiosapiens.controlador;

import com.colegiosapiens.dto.SgaDto;
import com.colegiosapiens.servico.SgaServico;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Sincronização com o SGA (/api/sga), para secretaria e administrador. */
@RestController
@RequestMapping("/api/sga")
public class SgaController {

    private final SgaServico servico;

    public SgaController(SgaServico servico) {
        this.servico = servico;
    }

    @GetMapping
    public SgaDto.Resumo resumo() {
        return servico.resumo();
    }

    @PostMapping("/sincronizar")
    public SgaDto.Registo sincronizar() {
        return servico.sincronizar();
    }
}
