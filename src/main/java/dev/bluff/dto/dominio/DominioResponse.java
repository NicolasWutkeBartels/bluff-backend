package dev.bluff.dto.dominio;

import dev.bluff.model.Status;

public record DominioResponse(String codigo, String descricao) {

    public static DominioResponse de(Status status) {
        return new DominioResponse(status.getCodigo(), status.getDescricao());
    }
}
