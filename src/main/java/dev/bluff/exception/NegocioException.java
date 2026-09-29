package dev.bluff.exception;

public class NegocioException extends RuntimeException {

    private final TipoErro tipo;

    public NegocioException(TipoErro tipo, Object... argumentos) {
        super(tipo.formatar(argumentos));
        this.tipo = tipo;
    }

    public TipoErro getTipo() {
        return tipo;
    }
}
