package com.colegiosapiens.servico;

import com.colegiosapiens.dto.ProfessorDto;
import com.colegiosapiens.excepcao.RegraNegocioException;
import com.colegiosapiens.modelo.Professor;
import com.colegiosapiens.repositorio.ProfessorRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** US01: cadastro e consulta de professores. */
@Service
public class ProfessorServico {

    private final ProfessorRepositorio repositorio;
    private final UtilizadorServico utilizadorServico;

    public ProfessorServico(ProfessorRepositorio repositorio, UtilizadorServico utilizadorServico) {
        this.repositorio = repositorio;
        this.utilizadorServico = utilizadorServico;
    }

    @Transactional
    public ProfessorDto.Resposta cadastrar(ProfessorDto.Requisicao dados) {
        String codigo = dados.codigoDocente().trim();
        if (repositorio.existsByCodigoDocente(codigo)) {
            throw new RegraNegocioException("Já existe um professor com este código de docente.");
        }

        Professor professor = new Professor();
        utilizadorServico.preencherContaBase(professor, dados.nomeCompleto(),
                dados.email(), dados.telefone(), dados.senha());
        professor.setCodigoDocente(codigo);
        professor.setEspecialidade(dados.especialidade());

        return ProfessorDto.Resposta.de(repositorio.save(professor));
    }

    @Transactional(readOnly = true)
    public List<ProfessorDto.Resposta> listar() {
        return repositorio.findAll().stream().map(ProfessorDto.Resposta::de).toList();
    }
}
