package dev.bluff.evento;

import java.util.UUID;

/**
 * Algo que aconteceu numa sala e que os clientes conectados precisam saber.
 *
 * <p>Os services publicam este objeto como evento do Spring ({@code ApplicationEventPublisher});
 * o {@link PublicadorDeEventos} decide quando e como mandá-lo pelo WebSocket.
 * Assim os services não conhecem STOMP.
 */
public record EventoSala(String codigoSala, TipoEvento tipo, Object dados) {

    public record VotoRegistrado(UUID rodadaId, UUID participanteId) {
    }

    public record ParticipanteSaiu(UUID participanteId) {
    }

    /** Formato que chega ao cliente: {@code {"tipo": "...", "dados": {...}}}. */
    public record Mensagem(TipoEvento tipo, Object dados) {
    }
}
