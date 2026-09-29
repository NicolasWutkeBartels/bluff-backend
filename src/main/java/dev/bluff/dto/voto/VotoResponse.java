package dev.bluff.dto.voto;

import dev.bluff.model.Voto;

import java.util.UUID;

public record VotoResponse(UUID participanteId, String valor) {

    public static VotoResponse oculto(Voto voto) {
        return new VotoResponse(voto.getParticipante().getId(), null);
    }

    public static VotoResponse revelado(Voto voto) {
        return new VotoResponse(voto.getParticipante().getId(), voto.getValor());
    }
}
