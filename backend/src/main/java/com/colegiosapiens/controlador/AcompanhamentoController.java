package com.colegiosapiens.controlador;

import com.colegiosapiens.dto.AlunoDto;
import com.colegiosapiens.dto.NotaDto;
import com.colegiosapiens.dto.SolicitacaoDto;
import com.colegiosapiens.excepcao.RegraNegocioException;
import com.colegiosapiens.servico.AcompanhamentoServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Portal do encarregado (/api/portal/**, só para encarregados) e lançamentos de notas e pedidos de reunião. */
@RestController
@RequestMapping("/api")
public class AcompanhamentoController {

    private final AcompanhamentoServico servico;

    public AcompanhamentoController(AcompanhamentoServico servico) {
        this.servico = servico;
    }

    @GetMapping("/portal/educandos")
    public List<AlunoDto.Resposta> educandos(Authentication autenticacao) {
        return servico.educandos(Long.valueOf(autenticacao.getName()));
    }

    @GetMapping("/portal/educandos/{alunoId}/notas")
    public List<NotaDto.Linha> notas(Authentication autenticacao, @PathVariable Long alunoId,
                                     @RequestParam(defaultValue = "1") int trimestre) {
        return servico.notasDoEducando(Long.valueOf(autenticacao.getName()), alunoId, trimestre);
    }

    @GetMapping("/portal/notificacoes")
    public List<SolicitacaoDto.Resposta> notificacoes(Authentication autenticacao) {
        return servico.notificacoes(Long.valueOf(autenticacao.getName()));
    }

    @PutMapping("/portal/notificacoes/{id}/lida")
    public SolicitacaoDto.Resposta abrir(Authentication autenticacao, @PathVariable Long id) {
        return servico.abrirNotificacao(Long.valueOf(autenticacao.getName()), id);
    }

    @PostMapping("/notas")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> lancarNota(@Valid @RequestBody NotaDto.Requisicao dados) {
        servico.lancarNota(dados);
        return Map.of("mensagem", "Nota registada.");
    }

    @PostMapping("/solicitacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> solicitar(Authentication autenticacao, @Valid @RequestBody SolicitacaoDto.Requisicao dados) {
        boolean professor = autenticacao.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PROFESSOR"));
        Long professorId = professor ? Long.valueOf(autenticacao.getName()) : dados.professorId();
        if (professorId == null) {
            throw new RegraNegocioException("Indique o professor.");
        }
        int criadas = servico.solicitarReuniao(professorId, dados);
        return Map.of("mensagem", "Pedido enviado a " + criadas + " encarregado(s).");
    }
}
