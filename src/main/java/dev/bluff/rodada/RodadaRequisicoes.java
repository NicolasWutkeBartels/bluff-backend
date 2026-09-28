package dev.bluff.rodada;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class RodadaRequisicoes {

    private RodadaRequisicoes() {
    }

    public record Iniciar(
            @NotBlank(message = "Diga o que vai ser estimado nesta rodada.")
            @Size(max = 200, message = "O título pode ter até 200 caracteres.")
            String titulo) {
    }

    public record Votar(
            @NotBlank(message = "Escolha uma carta.")
            @Size(max = 10, message = "Carta inválida.")
            String valor) {
    }

    /** A estimativa final é opcional: dá para revelar e decidir depois. */
    public record Revelar(
            @Size(max = 10, message = "Estimativa inválida.")
            String estimativaFinal) {
    }
}
