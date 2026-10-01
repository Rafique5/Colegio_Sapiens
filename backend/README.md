# Colegio Sapiens - Backend (Sistema de Comunicação e Acompanhamento Escolar)

Aplicação web em Java Spring Boot (API REST) com frontend em HTML, CSS e JavaScript.
Esta versão cobre a Sprint 1: US32, US33, US28, US27, US26, US01 e US11.

## Requisitos
- JDK 21 ou superior
- Maven 3.9+ (ou usar o Maven da IDE)

## Como executar
1. Abrir a pasta na IDE (IntelliJ, Eclipse, VS Code) como projecto Maven.
2. Executar a classe `ColegioSapiensApplication`, ou na linha de comandos: `mvn spring-boot:run`
3. Abrir http://localhost:8080
4. Entrar com o administrador inicial (definido em `application.properties`):
   - E-mail: `admin@sapiens.co.mz`
   - Senha: `Admin@12345`  (alterar assim que possível)

A base de dados H2 (por omissão) fica na pasta `dados/`. Para MySQL, ver os comentários em `application.properties` e pôr a senha real.

Se o computador tiver pouca memória (~1 GB), o `pom.xml` já limita o Java (heap 384 MB e SerialGC) quando se usa `mvn spring-boot:run`. No NetBeans, em *Properties → Actions → Run project*, use `spring-boot:run` em *Execute Goals*.

Frontend separado (pasta `frontend`): as origens permitidas ficam em `sapiens.cors.origens`.

## Estrutura de pacotes (com.colegiosapiens)
| Pacote | Conteúdo |
|---|---|
| modelo | Entidades JPA (Utilizador e subclasses, Turma, VinculoParental) |
| repositorio | Interfaces de acesso à base de dados |
| dto | Objectos de entrada e saída da API (com validações) |
| servico | Regras de negócio |
| controlador | Endpoints REST |
| seguranca / config | Login, permissões por perfil e administrador inicial |
| excepcao | Tratamento central de erros |

## Endpoints da Sprint 1
| Método e rota | US | Perfis |
|---|---|---|
| POST /api/auth/login (campos: identificador, senha) | US32 | público |
| POST /api/auth/logout, GET /api/auth/eu | US32 | autenticado |
| POST, GET /api/utilizadores | US33 | ADMINISTRADOR |
| POST, GET /api/turmas ; GET /api/turmas/{id}/alunos | US28 | POST: ADMINISTRADOR; GET: ADMINISTRADOR, SECRETARIA |
| POST, GET /api/alunos | US27 | ADMINISTRADOR, SECRETARIA |
| POST, GET /api/encarregados | US26 | ADMINISTRADOR, SECRETARIA |
| POST, GET /api/professores | US01 | POST: ADMINISTRADOR; GET: ADMINISTRADOR, SECRETARIA |
| POST /api/vinculos ; GET /api/vinculos/encarregado/{id} ; GET /api/vinculos/aluno/{id} | US11 | ADMINISTRADOR, SECRETARIA |

## Exemplos de corpo JSON
Criar secretaria (POST /api/utilizadores):
```json
{ "nomeCompleto": "Maria Macie", "email": "secretaria@sapiens.co.mz",
  "senha": "Secret@2026", "perfil": "SECRETARIA", "cargoOuSetor": "Secretaria geral" }
```
Criar turma (POST /api/turmas):
```json
{ "nomeTurma": "10ª Classe - Turma A", "anoLectivo": 2026, "sala": "Sala 4" }
```
Criar aluno (POST /api/alunos):
```json
{ "nomeCompleto": "Joao Sitoe", "codigoEstudante": "2026-001",
  "dataNascimento": "2010-05-14", "turmaId": 1 }
```
Criar encarregado (POST /api/encarregados):
```json
{ "nomeCompleto": "Ana Sitoe", "telefone": "841234567", "senha": "Ana@2026x" }
```
Associar (POST /api/vinculos):
```json
{ "alunoId": 3, "encarregadoId": 4 }
```

## Notas
- Login por e-mail ou telefone (o encarregado pode não ter e-mail).
- CSRF está desactivado para simplificar; activar em produção.
- Matricula, Disciplina e AlocacaoDocente do diagrama de classes entram nas próximas sprints.
