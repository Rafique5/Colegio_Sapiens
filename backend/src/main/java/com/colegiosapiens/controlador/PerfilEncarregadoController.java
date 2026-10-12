package com.colegiosapiens.controlador;

import com.colegiosapiens.dto.PerfilEncarregadoDto;
import com.colegiosapiens.servico.PerfilEncarregadoServico;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Perfil do encarregado (/api/encarregados/{id}/perfil), para secretaria e administrador. */
@RestController
@RequestMapping("/api/encarregados/{id}/perfil")
public class PerfilEncarregadoController {

    private final PerfilEncarregadoServico servico;

    public PerfilEncarregadoController(PerfilEncarregadoServico servico) {
        this.servico = servico;
    }

    @GetMapping
    public PerfilEncarregadoDto.Resposta obter(@PathVariable Long id) {
        return servico.obter(id);
    }

    @PutMapping
    public PerfilEncarregadoDto.Resposta guardar(@PathVariable Long id,
                                                 @Valid @RequestBody PerfilEncarregadoDto.Requisicao dados) {
        return servico.guardar(id, dados);
    }
}
