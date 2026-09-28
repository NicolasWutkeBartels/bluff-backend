package dev.bluff.participante;

import java.util.UUID;

public record ParticipanteResposta(UUID id, String apelido, Papel papel) {

    public static ParticipanteResposta de(Participante participante) {
        return new ParticipanteResposta(participante.getId(), participante.getApelido(), participante.getPapel());
    }
}
