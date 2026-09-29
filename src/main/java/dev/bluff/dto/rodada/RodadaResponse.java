package dev.bluff.dto.rodada;

import dev.bluff.dto.dominio.DominioResponse;
import dev.bluff.dto.voto.VotoResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RodadaResponse(
        UUID id,
        String titulo,
        DominioResponse status,
        String estimativaFinal,
        Instant criadoEm,
        List<VotoResponse> votos) {
}
