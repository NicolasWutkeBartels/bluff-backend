package dev.bluff;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
public abstract class IntegracaoTestBase {

    protected static final String PARTICIPANTE = "X-Participante-Id";

    @Value("${local.server.port}")
    protected int porta;

    private RestClient http;

    @BeforeEach
    void prepararCliente() {
        http = RestClient.builder()
                .baseUrl("http://localhost:" + porta)
                .defaultStatusHandler(HttpStatusCode::isError, (requisicao, resposta) -> {
                })
                .build();
    }

    protected record Resposta(int status, String corpo) {
        public <T> T json(String caminho) {
            return JsonPath.read(corpo, caminho);
        }
    }

    protected Resposta get(String caminho) {
        var resposta = http.get().uri(caminho).retrieve().toEntity(String.class);
        return new Resposta(resposta.getStatusCode().value(), resposta.getBody());
    }

    protected Resposta post(String caminho, String participanteId, String corpoJson) {
        var requisicao = http.post().uri(caminho).contentType(MediaType.APPLICATION_JSON);
        if (participanteId != null) {
            requisicao.header(PARTICIPANTE, participanteId);
        }
        if (corpoJson != null) {
            requisicao.body(corpoJson);
        }
        var resposta = requisicao.retrieve().toEntity(String.class);
        return new Resposta(resposta.getStatusCode().value(), resposta.getBody());
    }

    protected String[] criarSala(String apelido) {
        Resposta resposta = post("/salas", null, """
                {"nomeSala": "Sala de teste", "apelido": "%s"}""".formatted(apelido));
        return new String[] {resposta.json("$.codigoSala"), resposta.json("$.participante.id")};
    }

    protected String entrar(String codigoSala, String apelido, String papel) {
        Resposta resposta = post("/salas/" + codigoSala + "/participantes", null, """
                {"apelido": "%s", "papel": "%s"}""".formatted(apelido, papel));
        return resposta.json("$.participante.id");
    }

    protected String iniciarRodada(String codigoSala, String participanteId, String titulo) {
        Resposta resposta = post("/salas/" + codigoSala + "/rodadas", participanteId, """
                {"titulo": "%s"}""".formatted(titulo));
        return resposta.json("$.id");
    }

    protected Resposta votar(String rodadaId, String participanteId, String valor) {
        return post("/rodadas/" + rodadaId + "/votos", participanteId, """
                {"valor": "%s"}""".formatted(valor));
    }

    protected Resposta revelar(String rodadaId, String participanteId) {
        return post("/rodadas/" + rodadaId + "/revelar", participanteId, null);
    }
}
