package dev.bluff;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SalaTest extends IntegracaoTestBase {

    @Test
    void criarSalaDevolveCodigoEPrimeiroParticipante() {
        Resposta resposta = post("/salas", null, """
                {"nomeSala": "Time Azul", "apelido": "Ana", "baralho": "CAMISETA"}""");

        assertThat(resposta.status()).isEqualTo(201);
        assertThat(resposta.<String>json("$.codigoSala")).matches("[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{6}");
        assertThat(resposta.<String>json("$.participante.papel")).isEqualTo("VOTANTE");

        Resposta estado = get("/salas/" + resposta.json("$.codigoSala"));
        assertThat(estado.<String>json("$.baralho.codigo")).isEqualTo("CAMISETA");
    }

    @Test
    void codigoDaSalaAceitaMinusculas() {
        String[] sala = criarSala("Ana");

        assertThat(get("/salas/" + sala[0].toLowerCase()).status()).isEqualTo(200);
    }

    @Test
    void apelidoRepetidoNaMesmaSalaERecusado() {
        String[] sala = criarSala("Ana");

        Resposta resposta = post("/salas/" + sala[0] + "/participantes", null, """
                {"apelido": "Ana"}""");

        assertThat(resposta.status()).isEqualTo(409);
        assertThat(resposta.<String>json("$.codigo")).isEqualTo("APELIDO_EM_USO");
    }

    @Test
    void oMesmoApelidoPodeExistirEmSalasDiferentes() {
        criarSala("Ana");
        String[] outra = criarSala("Ana");

        assertThat(get("/salas/" + outra[0]).<String>json("$.participantes[0].apelido")).isEqualTo("Ana");
    }

    @Test
    void soExisteUmaRodadaEmVotacaoPorSala() {
        String[] sala = criarSala("Ana");
        iniciarRodada(sala[0], sala[1], "Primeira");

        Resposta segunda = post("/salas/" + sala[0] + "/rodadas", sala[1], """
                {"titulo": "Segunda"}""");

        assertThat(segunda.status()).isEqualTo(409);
        assertThat(segunda.<String>json("$.codigo")).isEqualTo("RODADA_EM_ANDAMENTO");
    }

    @Test
    void camposObrigatoriosVoltamComMensagemPorCampo() {
        Resposta resposta = post("/salas", null, """
                {"nomeSala": " ", "apelido": ""}""");

        assertThat(resposta.status()).isEqualTo(400);
        assertThat(resposta.<String>json("$.codigo")).isEqualTo("VALIDACAO");
        assertThat(resposta.<String>json("$.campos.nomeSala")).isNotBlank();
        assertThat(resposta.<String>json("$.campos.apelido")).isNotBlank();
    }

    @Test
    void salaInexistenteResponde404() {
        Resposta resposta = get("/salas/ZZZZZZ");

        assertThat(resposta.status()).isEqualTo(404);
        assertThat(resposta.<String>json("$.codigo")).isEqualTo("SALA_NAO_ENCONTRADA");
    }
}
