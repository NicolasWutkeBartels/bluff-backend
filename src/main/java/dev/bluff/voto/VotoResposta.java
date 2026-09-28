package dev.bluff.voto;

import java.util.UUID;

/**
 * Um voto como aparece para o cliente. Enquanto a rodada está em votação, {@code valor} é sempre nulo:
 * o cliente só sabe <em>quem</em> votou. Use as fábricas abaixo em vez do construtor.
 */
public record VotoResposta(UUID participanteId, String valor) {

    public static VotoResposta oculto(Voto voto) {
        return new VotoResposta(voto.getParticipante().getId(), null);
    }

    public static VotoResposta revelado(Voto voto) {
        return new VotoResposta(voto.getParticipante().getId(), voto.getValor());
    }
}
