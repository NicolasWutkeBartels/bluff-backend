package dev.bluff.dto.sala;

import dev.bluff.model.enums.Baralho;
import dev.bluff.model.enums.Papel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarSalaRequest(
        @NotBlank(message = "Dê um nome para a sala.")
        @Size(max = 80, message = "O nome da sala pode ter até 80 caracteres.")
        String nomeSala,

        @NotBlank(message = "Escolha um apelido.")
        @Size(max = 30, message = "O apelido pode ter até 30 caracteres.")
        String apelido,

        Baralho baralho,
        Papel papel) {
}
