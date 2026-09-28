package dev.bluff.erro;

/**
 * Exceção lançada pelos services quando uma regra de negócio é violada.
 * O {@link TratadorDeErros} a transforma numa resposta HTTP padronizada.
 */
public class ErroNegocio extends RuntimeException {

    private final TipoErro tipo;

    public ErroNegocio(TipoErro tipo, Object... argumentos) {
        super(tipo.formatar(argumentos));
        this.tipo = tipo;
    }

    public TipoErro getTipo() {
        return tipo;
    }
}
