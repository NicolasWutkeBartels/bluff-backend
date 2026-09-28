package dev.bluff.sala;

import dev.bluff.participante.ParticipanteResposta;
import dev.bluff.rodada.RodadaResposta;

import java.util.List;

public final class SalaRespostas {

    private SalaRespostas() {
    }

    /**
     * Devolvida ao criar ou entrar numa sala. O {@code participante.id} é a "chave" da pessoa na sala:
     * o front guarda no localStorage e envia no cabeçalho X-Participante-Id.
     */
    public record Entrada(String codigoSala, ParticipanteResposta participante) {
    }

    /** Foto atual da sala. É o que o front busca ao abrir a tela e ao reconectar o WebSocket. */
    public record Estado(
            String codigo,
            String nome,
            BaralhoResposta baralho,
            List<ParticipanteResposta> participantes,
            RodadaResposta rodadaAtual) {
    }
}
