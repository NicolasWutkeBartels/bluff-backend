package dev.bluff.sala;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Teste de unidade: não precisa de Spring nem de banco. */
class GeradorCodigoSalaTest {

    private final GeradorCodigoSala gerador = new GeradorCodigoSala();

    @Test
    void geraSeisCaracteresSemLetrasOuNumerosAmbiguos() {
        for (int i = 0; i < 10_000; i++) {
            assertThat(gerador.gerar())
                    .hasSize(6)
                    .doesNotContainAnyWhitespaces()
                    .matches("[^0O1IL]{6}")
                    .matches("[" + GeradorCodigoSala.ALFABETO + "]{6}");
        }
    }
}
