package dev.bluff.status;

/**
 * Um item de select. O front envia de volta o {@code codigo}, nunca o id:
 * o código é estável entre ambientes, o UUID não.
 */
public record DominioResposta(String codigo, String descricao) {

    public static DominioResposta de(Status status) {
        return new DominioResposta(status.getCodigo(), status.getDescricao());
    }
}
