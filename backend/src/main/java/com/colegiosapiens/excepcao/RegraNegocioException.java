package com.colegiosapiens.excepcao;

/**
 * Lançada quando um pedido viola uma regra do sistema
 * (ex.: e-mail já registado, turma duplicada). Resulta em HTTP 400.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
