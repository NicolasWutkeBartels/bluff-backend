package dev.bluff.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodigoSalaGeneratorTest {

    private final CodigoSalaGenerator gerador = new CodigoSalaGenerator();

    @Test
    void geraSeisCaracteresSemLetrasOuNumerosAmbiguos() {
        for (int i = 0; i < 10_000; i++) {
            assertThat(gerador.gerar())
                    .hasSize(6)
                    .doesNotContainAnyWhitespaces()
                    .matches("[^0O1IL]{6}")
                    .matches("[" + CodigoSalaGenerator.ALFABETO + "]{6}");
        }
    }
}
