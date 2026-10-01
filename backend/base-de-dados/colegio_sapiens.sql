-- =====================================================================
-- Base de dados do sistema Colegio Sapiens (MySQL 8+)
-- Sprint 1: utilizadores, turmas, alunos, encarregados e vinculos
-- Executar uma unica vez, com um utilizador que tenha permissao para
-- criar bases de dados (ex.: root).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS colegio_sapiens
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE colegio_sapiens;

-- ---------------------------------------------------------------------
-- Tabela base: qualquer pessoa com registo no sistema
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS utilizadores (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    nome_completo VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NULL,
    telefone      VARCHAR(30)  NULL,
    senha_hash    VARCHAR(100) NULL,            -- senha codificada com BCrypt
    ativo         BIT          NOT NULL DEFAULT 1,
    perfil        VARCHAR(20)  NOT NULL,        -- ADMINISTRADOR, SECRETARIA, PROFESSOR, ENCARREGADO, ALUNO
    data_criacao  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_utilizadores_email (email),
    UNIQUE KEY uk_utilizadores_telefone (telefone)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabelas especificas de cada tipo de utilizador (mesmo id da tabela base)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS administradores (
    id    BIGINT       NOT NULL,
    cargo VARCHAR(100) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_administradores_utilizador FOREIGN KEY (id) REFERENCES utilizadores (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS secretarias (
    id    BIGINT       NOT NULL,
    setor VARCHAR(100) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_secretarias_utilizador FOREIGN KEY (id) REFERENCES utilizadores (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS professores (
    id             BIGINT       NOT NULL,
    codigo_docente VARCHAR(30)  NOT NULL,
    especialidade  VARCHAR(100) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_professores_codigo (codigo_docente),
    CONSTRAINT fk_professores_utilizador FOREIGN KEY (id) REFERENCES utilizadores (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS encarregados (
    id                              BIGINT NOT NULL,
    notificacao_boas_vindas_enviada BIT    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_encarregados_utilizador FOREIGN KEY (id) REFERENCES utilizadores (id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Turmas (nome unico dentro do mesmo ano lectivo)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS turmas (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    nome_turma  VARCHAR(50) NOT NULL,
    ano_lectivo INT         NOT NULL,
    sala        VARCHAR(50) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_turmas_nome_ano (nome_turma, ano_lectivo)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Alunos (cada aluno pertence a uma turma)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS alunos (
    id               BIGINT      NOT NULL,
    codigo_estudante VARCHAR(30) NOT NULL,
    data_nascimento  DATE        NOT NULL,
    turma_id         BIGINT      NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_alunos_codigo (codigo_estudante),
    CONSTRAINT fk_alunos_utilizador FOREIGN KEY (id) REFERENCES utilizadores (id),
    CONSTRAINT fk_alunos_turma FOREIGN KEY (turma_id) REFERENCES turmas (id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Vinculo entre aluno e encarregado de educacao
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vinculos_parentais (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    aluno_id        BIGINT NOT NULL,
    encarregado_id  BIGINT NOT NULL,
    data_associacao DATE   NOT NULL,
    ativo           BIT    NOT NULL DEFAULT 1,
    parentesco      VARCHAR(30) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vinculo_aluno_encarregado (aluno_id, encarregado_id),
    CONSTRAINT fk_vinculo_aluno FOREIGN KEY (aluno_id) REFERENCES alunos (id),
    CONSTRAINT fk_vinculo_encarregado FOREIGN KEY (encarregado_id) REFERENCES encarregados (id)
) ENGINE=InnoDB;

-- O administrador inicial NAO e criado aqui: a aplicacao cria-o sozinha
-- no primeiro arranque (com a senha ja codificada), a partir do
-- application.properties.
