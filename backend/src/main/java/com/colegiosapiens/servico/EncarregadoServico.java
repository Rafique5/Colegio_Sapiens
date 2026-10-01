package com.colegiosapiens.servico;

import com.colegiosapiens.dto.EncarregadoDto;
import com.colegiosapiens.modelo.EncarregadoEducacao;
import com.colegiosapiens.repositorio.EncarregadoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** US26: cadastro e consulta de encarregados de educação. */
@Service
public class EncarregadoServico {

    private final EncarregadoRepositorio repositorio;
    private final UtilizadorServico utilizadorServico;

    public EncarregadoServico(EncarregadoRepositorio repositorio, UtilizadorServico utilizadorServico) {
        this.repositorio = repositorio;
        this.utilizadorServico = utilizadorServico;
    }

    @Transactional
    public EncarregadoDto.Resposta cadastrar(EncarregadoDto.Requisicao dados) {
        EncarregadoEducacao encarregado = new EncarregadoEducacao();
        utilizadorServico.preencherContaBase(encarregado, dados.nomeCompleto(),
                dados.email(), dados.telefone(), dados.senha());
        return EncarregadoDto.Resposta.de(repositorio.save(encarregado));
    }

    @Transactional(readOnly = true)
    public List<EncarregadoDto.Resposta> listar() {
        return repositorio.findAll().stream().map(EncarregadoDto.Resposta::de).toList();
    }
}
