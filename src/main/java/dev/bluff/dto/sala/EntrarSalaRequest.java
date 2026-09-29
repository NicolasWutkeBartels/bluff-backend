package dev.bluff.dto.sala;

import dev.bluff.model.enums.Papel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EntrarSalaRequest(
        @NotBlank(message = "Escolha um apelido.")
        @Size(max = 30, message = "O apelido pode ter até 30 caracteres.")
        String apelido,

        Papel papel) {
}
