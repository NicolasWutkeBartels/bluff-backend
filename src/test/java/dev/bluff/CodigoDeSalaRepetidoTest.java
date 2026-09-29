package dev.bluff;

import dev.bluff.util.CodigoSalaGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CodigoDeSalaRepetidoTest extends IntegracaoTestBase {

    @MockitoBean
    CodigoSalaGenerator gerador;

    @Test
    void quandoOCodigoJaExisteGeraOutroETentaDeNovo() {
        when(gerador.gerar()).thenReturn("AAAAAA", "AAAAAA", "AAAAAA", "BBBBBB");

        Resposta primeira = post("/salas", null, """
                {"nomeSala": "Primeira", "apelido": "Ana"}""");
        Resposta segunda = post("/salas", null, """
                {"nomeSala": "Segunda", "apelido": "Bia"}""");

        assertThat(primeira.<String>json("$.codigoSala")).isEqualTo("AAAAAA");
        assertThat(segunda.status()).isEqualTo(201);
        assertThat(segunda.<String>json("$.codigoSala")).isEqualTo("BBBBBB");
        assertThat(get("/salas/BBBBBB").<String>json("$.nome")).isEqualTo("Segunda");
    }

    @Test
    void desisteDepoisDeCincoColisoes() {
        when(gerador.gerar()).thenReturn("CCCCCC");
        post("/salas", null, """
                {"nomeSala": "Dona do código", "apelido": "Ana"}""");

        Resposta resposta = post("/salas", null, """
                {"nomeSala": "Azarada", "apelido": "Bia"}""");

        assertThat(resposta.status()).isEqualTo(503);
        assertThat(resposta.<String>json("$.codigo")).isEqualTo("CODIGO_SALA_INDISPONIVEL");
        verify(gerador, times(6)).gerar();
    }
}
