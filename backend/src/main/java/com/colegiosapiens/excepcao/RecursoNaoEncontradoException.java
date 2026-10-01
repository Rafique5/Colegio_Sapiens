package com.colegiosapiens.excepcao;

/**
 * Lançada quando um registo pedido não existe (ex.: turma com id inexistente).
 * Resulta em HTTP 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
