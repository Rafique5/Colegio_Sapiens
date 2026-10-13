package com.colegiosapiens.sga;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * SGA de demonstração: devolve sempre os mesmos dados. Serve para mostrar a sincronização
 * enquanto não existe ligação ao SGA real. Com sapiens.sga.simular-falha=true simula um SGA indisponível.
 */
@Component
public class SgaSimulado implements SgaFonte {

    private final boolean simularFalha;

    public SgaSimulado(@Value("${sapiens.sga.simular-falha:false}") boolean simularFalha) {
        this.simularFalha = simularFalha;
    }

    @Override
    public SgaDados obter() {
        if (simularFalha) {
            throw new IllegalStateException("SGA indisponível");
        }
        int ano = LocalDate.now().getYear();
        return new SgaDados(
                List.of(new TurmaSga("10ª A", ano, "11"), new TurmaSga("10ª B", ano, "12"), new TurmaSga("11ª A", ano, "21")),
                List.of(new ProfessorSga("PROF-031", "Nuno Sitoe", "nuno.sitoe@colegiosapiens.co.mz", "Matemática"),
                        new ProfessorSga("PROF-040", "Rita Machava", "rita.machava@colegiosapiens.co.mz", "Português"),
                        new ProfessorSga("PROF-052", "Célia Machel", "celia.machel@colegiosapiens.co.mz", "Biologia")),
                List.of(new EncarregadoSga("Marta Cossa", "841234567", "marta.cossa@exemplo.co.mz"),
                        new EncarregadoSga("Joaquim Tembe", "842345678", "joaquim.tembe@exemplo.co.mz"),
                        new EncarregadoSga("Regina Mondlane", "823456789", "regina.mondlane@exemplo.co.mz"),
                        new EncarregadoSga("Paulo Nhaca", "874567890", "paulo.nhaca@exemplo.co.mz")),
                List.of(new AlunoSga("2024-0087", "Amélia Cossa", "amelia.cossa@alunos.colegiosapiens.co.mz", LocalDate.of(2010, 3, 12), "10ª A", ano),
                        new AlunoSga("2024-0112", "Bruno Machava", "bruno.machava@alunos.colegiosapiens.co.mz", LocalDate.of(2009, 7, 21), "10ª A", ano),
                        new AlunoSga("2024-0159", "Diana Tembe", "diana.tembe@alunos.colegiosapiens.co.mz", LocalDate.of(2010, 11, 2), "10ª B", ano),
                        new AlunoSga("2025-0203", "Pedro Cossa", "pedro.cossa@alunos.colegiosapiens.co.mz", LocalDate.of(2013, 5, 30), "11ª A", ano),
                        new AlunoSga("2023-0034", "Carlos Nhaca", "carlos.nhaca@alunos.colegiosapiens.co.mz", LocalDate.of(2008, 1, 15), "11ª A", ano)),
                List.of(new VinculoSga("2024-0087", "841234567", "Mãe"),
                        new VinculoSga("2025-0203", "841234567", "Mãe"),
                        new VinculoSga("2024-0159", "842345678", "Pai"),
                        new VinculoSga("2023-0034", "874567890", "Pai"),
                        new VinculoSga("2024-0112", "823456789", "Tio / Tia")),
                List.of(new DocenciaSga("PROF-031", "10ª A", ano), new DocenciaSga("PROF-031", "10ª B", ano),
                        new DocenciaSga("PROF-040", "10ª A", ano), new DocenciaSga("PROF-040", "11ª A", ano),
                        new DocenciaSga("PROF-052", "10ª B", ano)));
    }
}
