package dev.bluff.dto.voto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VotarRequest(
        @NotBlank(message = "Escolha uma carta.")
        @Size(max = 10, message = "Carta inválida.")
        String valor) {
}
