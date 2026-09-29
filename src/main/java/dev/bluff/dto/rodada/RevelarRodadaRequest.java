package dev.bluff.dto.rodada;

import jakarta.validation.constraints.Size;

public record RevelarRodadaRequest(
        @Size(max = 10, message = "Estimativa inválida.")
        String estimativaFinal) {
}
