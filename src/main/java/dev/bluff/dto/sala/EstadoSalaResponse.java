package dev.bluff.dto.sala;

import dev.bluff.dto.baralho.BaralhoResponse;
import dev.bluff.dto.participante.ParticipanteResponse;
import dev.bluff.dto.rodada.RodadaResponse;

import java.util.List;

public record EstadoSalaResponse(
        String codigo,
        String nome,
        BaralhoResponse baralho,
        List<ParticipanteResponse> participantes,
        RodadaResponse rodadaAtual) {
}
