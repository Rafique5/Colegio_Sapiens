package com.colegiosapiens.servico;

import com.colegiosapiens.dto.PerfilEncarregadoDto;
import com.colegiosapiens.excepcao.RecursoNaoEncontradoException;
import com.colegiosapiens.excepcao.RegraNegocioException;
import com.colegiosapiens.modelo.EncarregadoEducacao;
import com.colegiosapiens.modelo.VinculoParental;
import com.colegiosapiens.repositorio.EncarregadoRepositorio;
import com.colegiosapiens.repositorio.UtilizadorRepositorio;
import com.colegiosapiens.repositorio.VinculoParentalRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Perfil do encarregado: completar contactos e validar os vínculos com os educandos. */
@Service
public class PerfilEncarregadoServico {

    private final EncarregadoRepositorio encarregados;
    private final VinculoParentalRepositorio vinculos;
    private final UtilizadorRepositorio utilizadores;

    public PerfilEncarregadoServico(EncarregadoRepositorio encarregados, VinculoParentalRepositorio vinculos,
                                    UtilizadorRepositorio utilizadores) {
        this.encarregados = encarregados;
        this.vinculos = vinculos;
        this.utilizadores = utilizadores;
    }

    @Transactional(readOnly = true)
    public PerfilEncarregadoDto.Resposta obter(Long id) {
        return paraResposta(buscar(id));
    }

    @Transactional
    public PerfilEncarregadoDto.Resposta guardar(Long id, PerfilEncarregadoDto.Requisicao dados) {
        EncarregadoEducacao e = buscar(id);
        String email = vazio(dados.email()) ? null : dados.email().trim().toLowerCase();
        String telefone = vazio(dados.telefone()) ? null : dados.telefone().replaceAll("\\s+", "");
        if (email == null && telefone == null) {
            throw new RegraNegocioException("Indique pelo menos o telemóvel ou o e-mail.");
        }
        if (email != null) {
            utilizadores.findByEmail(email).filter(o -> !o.getId().equals(id))
                    .ifPresent(o -> { throw new RegraNegocioException("Esse e-mail já está em uso."); });
        }
        if (telefone != null) {
            utilizadores.findByTelefone(telefone).filter(o -> !o.getId().equals(id))
                    .ifPresent(o -> { throw new RegraNegocioException("Esse telemóvel já está em uso."); });
        }
        e.setEmail(email);
        e.setTelefone(telefone);
        e.setProfissao(vazio(dados.profissao()) ? null : dados.profissao().trim());

        Map<Long, Boolean> marcas = new HashMap<>();
        if (dados.vinculos() != null) {
            for (PerfilEncarregadoDto.Validacao x : dados.vinculos()) {
                marcas.put(x.vinculoId(), x.validado());
            }
        }
        for (VinculoParental v : vinculos.findByEncarregadoIdAndAtivoTrue(id)) {
            Boolean marca = marcas.get(v.getId());
            if (marca != null) {
                v.setValidado(marca);
            }
        }
        return paraResposta(e);
    }

    private EncarregadoEducacao buscar(Long id) {
        return encarregados.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encarregado não encontrado."));
    }

    private PerfilEncarregadoDto.Resposta paraResposta(EncarregadoEducacao e) {
        List<PerfilEncarregadoDto.Educando> educandos = vinculos.findByEncarregadoIdAndAtivoTrue(e.getId()).stream()
                .map(v -> new PerfilEncarregadoDto.Educando(v.getId(), v.getAluno().getId(),
                        v.getAluno().getNomeCompleto(), v.getAluno().getTurma().getNomeTurma(),
                        v.getParentesco(), v.isValidado()))
                .toList();
        return new PerfilEncarregadoDto.Resposta(e.getId(), e.getNomeCompleto(), e.getTelefone(), e.getEmail(),
                e.getProfissao(), educandos);
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}
