package dev.bluff.websocket;

import dev.bluff.dto.evento.TipoEvento;

public record EventoSala(String codigoSala, TipoEvento tipo, Object dados) {
}
