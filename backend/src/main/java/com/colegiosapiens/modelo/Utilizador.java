package com.colegiosapiens.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Classe abstracta que representa qualquer pessoa com registo no sistema.
 * Administrador, Secretaria, Professor, EncarregadoEducacao e Aluno herdam desta classe.
 *
 * Estratégia JOINED: os dados comuns ficam na tabela "utilizadores" e os dados
 * específicos de cada tipo ficam numa tabela própria, ligada pelo mesmo id.
 */
@Entity
@Table(name = "utilizadores")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Utilizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    /** Pode ser nulo (ex.: encarregados que só têm telefone). Quando existe, é único. */
    @Column(unique = true, length = 150)
    private String email;

    /** Pode ser nulo. Quando existe, é único (também serve para iniciar sessão). */
    @Column(unique = true, length = 30)
    private String telefone;

    /** Senha guardada sempre codificada (BCrypt). Nulo = utilizador sem acesso à plataforma. */
    @Column(name = "senha_hash", length = 100)
    private String senhaHash;

    /** Contas desactivadas não conseguem iniciar sessão, mas o histórico é preservado. */
    @Column(nullable = false)
    private boolean ativo = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    /** Construtor exigido pelo JPA. */
    protected Utilizador() {
    }

    /** Cada subclasse indica o seu perfil ao chamar este construtor. */
    protected Utilizador(Perfil perfil) {
        this.perfil = perfil;
    }

    /** Preenche a data de criação automaticamente antes de gravar pela primeira vez. */
    @PrePersist
    void aoCriar() {
        this.dataCriacao = LocalDateTime.now();
    }

    /** Desactiva a conta (não apaga dados). */
    public void desativarConta() {
        this.ativo = false;
    }

    /** Actualiza os contactos do utilizador. */
    public void atualizarContactos(String email, String telefone) {
        this.email = email;
        this.telefone = telefone;
    }

    // ---------------- Getters e setters ----------------

    public Long getId() { return id; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Perfil getPerfil() { return perfil; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
}
