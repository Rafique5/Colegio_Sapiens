package com.colegiosapiens.excepcao;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * Centraliza o tratamento de erros da API: transforma as excepções
 * em respostas JSON com uma mensagem clara em português.
 */
@RestControllerAdvice
public class ManipuladorExcepcoes {

    /** Formato único de resposta de erro. */
    public record ErroResposta(int estado, String mensagem, List<String> detalhes) {
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResposta(404, ex.getMessage(), List.of()));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> regraNegocio(RegraNegocioException ex) {
        return ResponseEntity.badRequest()
                .body(new ErroResposta(400, ex.getMessage(), List.of()));
    }

    /** Campos obrigatórios em falta ou com formato inválido (anotações @Valid). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest()
                .body(new ErroResposta(400, "Dados inválidos.", detalhes));
    }

    /** JSON malformado ou valor inválido (ex.: perfil que não existe, data mal escrita). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> corpoIlegivel(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ErroResposta(400, "O corpo do pedido é inválido ou está mal formatado.", List.of()));
    }

    /** Parâmetro de rota com tipo errado (ex.: /api/turmas/abc/alunos). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(new ErroResposta(400, "Parâmetro inválido: " + ex.getName() + ".", List.of()));
    }

    /** Última defesa contra duplicados que escapem às verificações dos serviços. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> integridade(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResposta(409, "Já existe um registo com estes dados.", List.of()));
    }
}
