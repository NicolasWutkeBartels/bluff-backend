package dev.bluff.dto.participante;

import dev.bluff.model.Participante;
import dev.bluff.model.enums.Papel;

import java.util.UUID;

public record ParticipanteResponse(UUID id, String apelido, Papel papel, boolean online) {

    public static ParticipanteResponse de(Participante participante, boolean online) {
        return new ParticipanteResponse(participante.getId(), participante.getApelido(), participante.getPapel(), online);
    }
}
