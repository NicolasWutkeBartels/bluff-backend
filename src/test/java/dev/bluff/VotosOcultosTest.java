package dev.bluff;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A regra central do Bluff: enquanto a rodada está em VOTING, nenhuma resposta (REST ou WebSocket)
 * pode conter o valor de um voto. Além de checar o campo {@code valor}, os testes procuram o valor
 * votado no texto da rodada, para pegar um vazamento em qualquer outro campo.
 * (O texto da sala inteira não serve: a lista de cartas do baralho contém esses valores.)
 */
class VotosOcultosTest extends TesteDeIntegracao {

    @Test
    void emVotacaoOEstadoDaSalaMostraQuemVotouMasNaoOValor() {
        String[] sala = criarSala("Ana");
        String bia = entrar(sala[0], "Bia", "VOTANTE");
        String rodada = iniciarRodada(sala[0], sala[1], "Checkout");
        votar(rodada, sala[1], "13");
        votar(rodada, bia, "21");

        Resposta estado = get("/salas/" + sala[0]);

        List<String> quemVotou = estado.json("$.rodadaAtual.votos[*].participanteId");
        List<Object> valores = estado.json("$.rodadaAtual.votos[*].valor");
        assertThat(quemVotou).containsExactlyInAnyOrder(sala[1], bia);
        assertThat(valores).containsOnlyNulls();
        assertThat(trechoDaRodada(estado.corpo())).doesNotContain("\"13\"", "\"21\"");
    }

    @Test
    void emVotacaoOHistoricoTambemEscondeOsValores() {
        String[] sala = criarSala("Ana");
        String rodada = iniciarRodada(sala[0], sala[1], "Checkout");
        votar(rodada, sala[1], "13");

        Resposta historico = get("/salas/" + sala[0] + "/rodadas");

        assertThat(historico.<List<Object>>json("$[0].votos[*].valor")).containsOnlyNulls();
        assertThat(historico.corpo()).doesNotContain("\"13\"");
    }

    @Test
    void depoisDeReveladaOsValoresAparecemNoEstadoNoHistoricoENaResposta() {
        String[] sala = criarSala("Ana");
        String bia = entrar(sala[0], "Bia", "VOTANTE");
        String rodada = iniciarRodada(sala[0], sala[1], "Checkout");
        votar(rodada, sala[1], "13");
        votar(rodada, bia, "21");

        Resposta revelacao = revelar(rodada, sala[1]);
        Resposta estado = get("/salas/" + sala[0]);
        Resposta historico = get("/salas/" + sala[0] + "/rodadas?status=REVEALED");

        assertThat(revelacao.<List<String>>json("$.votos[*].valor")).containsExactlyInAnyOrder("13", "21");
        assertThat(estado.<List<String>>json("$.rodadaAtual.votos[*].valor")).containsExactlyInAnyOrder("13", "21");
        assertThat(historico.<List<String>>json("$[0].votos[*].valor")).containsExactlyInAnyOrder("13", "21");
    }

    @Test
    void oEventoDeVotoPeloWebSocketNaoLevaOValorMasORevelarLeva() throws Exception {
        String[] sala = criarSala("Ana");
        String rodada = iniciarRodada(sala[0], sala[1], "Checkout");
        BlockingQueue<String> recebidas = assinarSala(sala[0]);

        votar(rodada, sala[1], "13");
        String eventoVoto = recebidas.poll(5, TimeUnit.SECONDS);

        assertThat(eventoVoto).contains("VOTO_REGISTRADO", sala[1]);
        assertThat(eventoVoto).doesNotContain("\"13\"", "valor");

        revelar(rodada, sala[1]);
        String eventoRevelacao = recebidas.poll(5, TimeUnit.SECONDS);

        assertThat(eventoRevelacao).contains("RODADA_REVELADA", "\"valor\":\"13\"");
    }

    private static String trechoDaRodada(String estadoDaSala) {
        return estadoDaSala.substring(estadoDaSala.indexOf("\"rodadaAtual\""));
    }

    /** Conecta um cliente STOMP de verdade e guarda o corpo de cada mensagem do tópico da sala. */
    private BlockingQueue<String> assinarSala(String codigoSala) throws Exception {
        WebSocketStompClient cliente = new WebSocketStompClient(new StandardWebSocketClient());
        StompSession sessao = cliente.connectAsync(
                        "ws://localhost:" + porta + "/ws", new WebSocketHttpHeaders(), new StompHeaders(),
                        new StompSessionHandlerAdapter() { })
                .get(5, TimeUnit.SECONDS);

        BlockingQueue<String> recebidas = new LinkedBlockingQueue<>();
        sessao.subscribe("/topic/salas/" + codigoSala, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                recebidas.add(new String((byte[]) payload, StandardCharsets.UTF_8));
            }
        });
        // Dá tempo de a assinatura chegar ao broker antes de o teste disparar eventos.
        Thread.sleep(300);
        return recebidas;
    }
}
