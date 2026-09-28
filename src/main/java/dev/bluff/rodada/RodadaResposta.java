package dev.bluff.rodada;

import dev.bluff.status.DominioResposta;
import dev.bluff.voto.VotoResposta;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Sempre criada pelo {@link MontadorDeRodada}, que aplica a regra dos votos ocultos. */
public record RodadaResposta(
        UUID id,
        String titulo,
        DominioResposta status,
        String estimativaFinal,
        Instant criadoEm,
        List<VotoResposta> votos) {
}
