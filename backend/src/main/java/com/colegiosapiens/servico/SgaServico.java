package com.colegiosapiens.servico;

import com.colegiosapiens.dto.SgaDto;
import com.colegiosapiens.modelo.Aluno;
import com.colegiosapiens.modelo.EncarregadoEducacao;
import com.colegiosapiens.modelo.Professor;
import com.colegiosapiens.modelo.SincronizacaoSga;
import com.colegiosapiens.modelo.Turma;
import com.colegiosapiens.modelo.Utilizador;
import com.colegiosapiens.modelo.VinculoParental;
import com.colegiosapiens.repositorio.AlunoRepositorio;
import com.colegiosapiens.repositorio.EncarregadoRepositorio;
import com.colegiosapiens.repositorio.ProfessorRepositorio;
import com.colegiosapiens.repositorio.SincronizacaoSgaRepositorio;
import com.colegiosapiens.repositorio.TurmaRepositorio;
import com.colegiosapiens.repositorio.UtilizadorRepositorio;
import com.colegiosapiens.repositorio.VinculoParentalRepositorio;
import com.colegiosapiens.sga.SgaFonte;
import com.colegiosapiens.sga.SgaFonte.AlunoSga;
import com.colegiosapiens.sga.SgaFonte.EncarregadoSga;
import com.colegiosapiens.sga.SgaFonte.ProfessorSga;
import com.colegiosapiens.sga.SgaFonte.SgaDados;
import com.colegiosapiens.sga.SgaFonte.TurmaSga;
import com.colegiosapiens.sga.SgaFonte.VinculoSga;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Sincronização com o SGA: traz turmas, professores, encarregados, alunos e vínculos.
 * Os utilizadores novos ficam sem senha (conta "Pendente") até a secretaria gerar as contas.
 */
@Service
public class SgaServico {

    private final SgaFonte fonte;
    private final SincronizacaoSgaRepositorio historico;
    private final TurmaRepositorio turmas;
    private final ProfessorRepositorio professores;
    private final AlunoRepositorio alunos;
    private final EncarregadoRepositorio encarregados;
    private final UtilizadorRepositorio utilizadores;
    private final VinculoParentalRepositorio vinculos;
    private final TransactionTemplate transacao;

    public SgaServico(SgaFonte fonte, SincronizacaoSgaRepositorio historico, TurmaRepositorio turmas,
                      ProfessorRepositorio professores, AlunoRepositorio alunos,
                      EncarregadoRepositorio encarregados, UtilizadorRepositorio utilizadores,
                      VinculoParentalRepositorio vinculos, PlatformTransactionManager gestor) {
        this.fonte = fonte;
        this.historico = historico;
        this.turmas = turmas;
        this.professores = professores;
        this.alunos = alunos;
        this.encarregados = encarregados;
        this.utilizadores = utilizadores;
        this.vinculos = vinculos;
        this.transacao = new TransactionTemplate(gestor);
    }

    public SgaDto.Resumo resumo() {
        return new SgaDto.Resumo(alunos.count(), turmas.count(), professores.count(), encarregados.count(),
                historico.findTop10ByOrderByDataHoraDesc().stream().map(SgaDto.Registo::de).toList());
    }

    /** Corre a sincronização e regista o resultado (também quando falha). */
    public SgaDto.Registo sincronizar() {
        LocalDateTime inicio = LocalDateTime.now();
        SincronizacaoSga registo;
        try {
            SgaDados dados = fonte.obter();
            Resultado r = transacao.execute(estado -> aplicar(dados));
            String mensagem = r.ignorados == 0 ? "Concluída sem erros"
                    : "Concluída; " + r.ignorados + " registo(s) ignorado(s) por conflito de dados";
            registo = new SincronizacaoSga(inicio, r.registos, r.novos, r.alterados, "CONCLUIDA", mensagem);
        } catch (Exception e) {
            String motivo = e.getMessage() == null ? "erro inesperado" : e.getMessage();
            registo = new SincronizacaoSga(inicio, 0, 0, 0, "FALHOU", motivo);
        }
        return SgaDto.Registo.de(historico.save(registo));
    }

    private static final class Resultado {
        int registos, novos, alterados, ignorados;
    }

    private Resultado aplicar(SgaDados d) {
        Resultado r = new Resultado();
        r.registos = d.turmas().size() + d.professores().size() + d.encarregados().size()
                + d.alunos().size() + d.vinculos().size();

        for (TurmaSga t : d.turmas()) {
            Optional<Turma> existente = turmas.findByNomeTurmaAndAnoLectivo(t.nome(), t.ano());
            if (existente.isEmpty()) {
                Turma nova = new Turma();
                nova.setNomeTurma(t.nome());
                nova.setAnoLectivo(t.ano());
                nova.setSala(t.sala());
                turmas.save(nova);
                r.novos++;
            } else if (!Objects.equals(existente.get().getSala(), t.sala())) {
                existente.get().setSala(t.sala());
                r.alterados++;
            }
        }

        for (ProfessorSga p : d.professores()) {
            Optional<Professor> existente = professores.findByCodigoDocente(p.codigo());
            if (existente.isPresent()) {
                Professor prof = existente.get();
                if (!Objects.equals(prof.getNomeCompleto(), p.nome()) || !Objects.equals(prof.getEspecialidade(), p.especialidade())) {
                    prof.setNomeCompleto(p.nome());
                    prof.setEspecialidade(p.especialidade());
                    r.alterados++;
                }
            } else if (utilizadores.existsByEmail(p.email())) {
                r.ignorados++;
            } else {
                Professor prof = new Professor();
                prof.setNomeCompleto(p.nome());
                prof.setEmail(p.email());
                prof.setCodigoDocente(p.codigo());
                prof.setEspecialidade(p.especialidade());
                professores.save(prof);
                r.novos++;
            }
        }

        for (EncarregadoSga e : d.encarregados()) {
            Optional<Utilizador> existente = utilizadores.findByTelefone(e.telefone());
            if (existente.isPresent()) {
                if (existente.get() instanceof EncarregadoEducacao enc) {
                    if (!Objects.equals(enc.getNomeCompleto(), e.nome())) {
                        enc.setNomeCompleto(e.nome());
                        r.alterados++;
                    }
                } else {
                    r.ignorados++;
                }
            } else {
                EncarregadoEducacao enc = new EncarregadoEducacao();
                enc.setNomeCompleto(e.nome());
                enc.setTelefone(e.telefone());
                if (e.email() != null && !utilizadores.existsByEmail(e.email())) {
                    enc.setEmail(e.email());
                }
                encarregados.save(enc);
                r.novos++;
            }
        }

        for (AlunoSga a : d.alunos()) {
            Optional<Turma> turma = turmas.findByNomeTurmaAndAnoLectivo(a.turma(), a.anoTurma());
            if (turma.isEmpty()) {
                r.ignorados++;
                continue;
            }
            Optional<Aluno> existente = alunos.findByCodigoEstudante(a.codigo());
            if (existente.isPresent()) {
                Aluno aluno = existente.get();
                boolean mudou = false;
                if (!Objects.equals(aluno.getNomeCompleto(), a.nome())) {
                    aluno.setNomeCompleto(a.nome());
                    mudou = true;
                }
                if (!Objects.equals(aluno.getTurma().getId(), turma.get().getId())) {
                    aluno.setTurma(turma.get());
                    mudou = true;
                }
                if (mudou) {
                    r.alterados++;
                }
            } else {
                Aluno aluno = new Aluno();
                aluno.setNomeCompleto(a.nome());
                if (a.email() != null && !utilizadores.existsByEmail(a.email())) {
                    aluno.setEmail(a.email());
                }
                aluno.setCodigoEstudante(a.codigo());
                aluno.setDataNascimento(a.nascimento());
                aluno.setTurma(turma.get());
                alunos.save(aluno);
                r.novos++;
            }
        }

        for (VinculoSga v : d.vinculos()) {
            Optional<Aluno> aluno = alunos.findByCodigoEstudante(v.codigoAluno());
            Optional<Utilizador> enc = utilizadores.findByTelefone(v.telefoneEncarregado());
            if (aluno.isEmpty() || enc.isEmpty() || !(enc.get() instanceof EncarregadoEducacao encarregado)) {
                r.ignorados++;
                continue;
            }
            if (!vinculos.existsByAlunoIdAndEncarregadoId(aluno.get().getId(), encarregado.getId())) {
                VinculoParental vinculo = new VinculoParental(aluno.get(), encarregado);
                vinculo.setParentesco(v.parentesco());
                vinculos.save(vinculo);
                r.novos++;
            }
        }
        return r;
    }
}
