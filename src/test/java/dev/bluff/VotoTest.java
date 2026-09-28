package dev.bluff;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VotoTest extends TesteDeIntegracao {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void votarDeNovoNaMesmaRodadaTrocaOVotoEmVezDeDuplicar() {
        String[] sala = criarSala("Ana");
        String ana = sala[1];
        String rodada = iniciarRodada(sala[0], ana, "Login");

        assertThat(votar(rodada, ana, "3").status()).isEqualTo(204);
        assertThat(votar(rodada, ana, "8").status()).isEqualTo(204);

        List<String> valores = jdbc.queryForList(
                "select vot_valor from voto where rod_id = ?", String.class, UUID.fromString(rodada));
        assertThat(valores).containsExactly("8");
    }

    @Test
    void oBancoRecusaUmSegundoVotoDoMesmoParticipanteNaMesmaRodada() {
        String[] sala = criarSala("Ana");
        String rodada = iniciarRodada(sala[0], sala[1], "Login");
        votar(rodada, sala[1], "5");

        // Contornando o service: a constraint uk_voto_rodada_participante é a última linha de defesa.
        assertThatThrownBy(() -> jdbc.update(
                "insert into voto (rod_id, par_id, vot_valor) values (?, ?, '13')",
                UUID.fromString(rodada), UUID.fromString(sala[1])))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uk_voto_rodada_participante");
    }

    @Test
    void observadorNaoVota() {
        String[] sala = criarSala("Ana");
        String caio = entrar(sala[0], "Caio", "OBSERVADOR");
        String rodada = iniciarRodada(sala[0], sala[1], "Login");

        Resposta resposta = votar(rodada, caio, "5");

        assertThat(resposta.status()).isEqualTo(403);
        assertThat(resposta.<String>json("$.codigo")).isEqualTo("OBSERVADOR_NAO_VOTA");
        assertThat(jdbc.queryForObject("select count(*) from voto where par_id = ?", Integer.class,
                UUID.fromString(caio))).isZero();
    }

    @Test
    void naoSeVotaNemSeRevelaUmaRodadaJaRevelada() {
        String[] sala = criarSala("Ana");
        String rodada = iniciarRodada(sala[0], sala[1], "Login");
        revelar(rodada, sala[1]);

        Resposta voto = votar(rodada, sala[1], "5");
        assertThat(voto.status()).isEqualTo(409);
        assertThat(voto.<String>json("$.codigo")).isEqualTo("RODADA_FECHADA_PARA_VOTOS");

        Resposta revelarDeNovo = revelar(rodada, sala[1]);
        assertThat(revelarDeNovo.status()).isEqualTo(409);
        assertThat(revelarDeNovo.<String>json("$.codigo")).isEqualTo("RODADA_JA_REVELADA");
    }

    @Test
    void cartaForaDoBaralhoERecusada() {
        String[] sala = criarSala("Ana");
        String rodada = iniciarRodada(sala[0], sala[1], "Login");

        Resposta resposta = votar(rodada, sala[1], "4");

        assertThat(resposta.status()).isEqualTo(400);
        assertThat(resposta.<String>json("$.codigo")).isEqualTo("CARTA_INVALIDA");
    }

    @Test
    void participanteDeOutraSalaNaoVota() {
        String[] salaA = criarSala("Ana");
        String[] salaB = criarSala("Bruno");
        String rodada = iniciarRodada(salaA[0], salaA[1], "Login");

        Resposta resposta = votar(rodada, salaB[1], "5");

        assertThat(resposta.status()).isEqualTo(403);
        assertThat(resposta.<String>json("$.codigo")).isEqualTo("PARTICIPANTE_FORA_DA_SALA");
    }
}
