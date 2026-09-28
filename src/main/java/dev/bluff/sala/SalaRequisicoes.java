package dev.bluff.sala;

import dev.bluff.participante.Papel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corpos das requisições de sala. Baralho e papel são opcionais e têm valor padrão. */
public final class SalaRequisicoes {

    private SalaRequisicoes() {
    }

    public record CriarSala(
            @NotBlank(message = "Dê um nome para a sala.")
            @Size(max = 80, message = "O nome da sala pode ter até 80 caracteres.")
            String nomeSala,

            @NotBlank(message = "Escolha um apelido.")
            @Size(max = 30, message = "O apelido pode ter até 30 caracteres.")
            String apelido,

            Baralho baralho,
            Papel papel) {
    }

    public record EntrarNaSala(
            @NotBlank(message = "Escolha um apelido.")
            @Size(max = 30, message = "O apelido pode ter até 30 caracteres.")
            String apelido,

            Papel papel) {
    }
}
