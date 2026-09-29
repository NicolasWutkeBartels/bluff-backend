package dev.bluff.dto.rodada;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IniciarRodadaRequest(
        @NotBlank(message = "Diga o que vai ser estimado nesta rodada.")
        @Size(max = 200, message = "O título pode ter até 200 caracteres.")
        String titulo) {
}
