package com.colegiosapiens.sga;

import java.time.LocalDate;
import java.util.List;

/**
 * Fonte de dados do SGA (Sistema de Gestão Académica).
 * Para ligar a um SGA real, basta criar outra implementação desta interface
 * (API ou leitura de ficheiro) e deixar a SgaSimulado de fora.
 */
public interface SgaFonte {

    /** Devolve todos os dados actuais do SGA, ou lança excepção se estiver indisponível. */
    SgaDados obter();

    record SgaDados(List<TurmaSga> turmas, List<ProfessorSga> professores, List<EncarregadoSga> encarregados,
                    List<AlunoSga> alunos, List<VinculoSga> vinculos) {
    }

    record TurmaSga(String nome, int ano, String sala) {
    }

    record ProfessorSga(String codigo, String nome, String email, String especialidade) {
    }

    /** O encarregado é identificado pelo telefone. */
    record EncarregadoSga(String nome, String telefone, String email) {
    }

    record AlunoSga(String codigo, String nome, String email, LocalDate nascimento, String turma, int anoTurma) {
    }

    record VinculoSga(String codigoAluno, String telefoneEncarregado, String parentesco) {
    }
}
