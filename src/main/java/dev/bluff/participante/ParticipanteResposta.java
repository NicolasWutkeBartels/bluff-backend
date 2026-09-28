package dev.bluff.participante;

import java.util.UUID;

/** {@code online} indica se a pessoa tem uma conexão WebSocket aberta agora. */
public record ParticipanteResposta(UUID id, String apelido, Papel papel, boolean online) {

    public static ParticipanteResposta de(Participante participante, boolean online) {
        return new ParticipanteResposta(participante.getId(), participante.getApelido(), participante.getPapel(), online);
    }
}
