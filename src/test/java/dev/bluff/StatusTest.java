package dev.bluff;

import dev.bluff.exception.NegocioException;
import dev.bluff.exception.TipoErro;
import dev.bluff.service.StatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusTest extends IntegracaoTestBase {

    @Autowired
    StatusService statusService;

    @Test
    void dominioRodadaTemOsDoisStatusDaMigration() {
        Resposta resposta = get("/dominios?dominio=RODADA");

        assertThat(resposta.<List<String>>json("$[*].codigo")).containsExactlyInAnyOrder("VOTING", "REVEALED");
    }

    @Test
    void dominioInexistenteDevolveListaVazia() {
        Resposta resposta = get("/dominios?dominio=NAO_EXISTE");

        assertThat(resposta.status()).isEqualTo(200);
        assertThat(resposta.corpo()).isEqualTo("[]");
    }

    @Test
    void buscarPorCodigoInexistenteLancaErroDeNegocio() {
        assertThatThrownBy(() -> statusService.buscarPorCodigo("RODADA", "PAUSADA"))
                .isInstanceOf(NegocioException.class)
                .extracting(erro -> ((NegocioException) erro).getTipo())
                .isEqualTo(TipoErro.STATUS_INEXISTENTE);
    }

    @Test
    void filtrarHistoricoPorStatusInexistenteResponde400() {
        String[] sala = criarSala("Ana");

        Resposta resposta = get("/salas/" + sala[0] + "/rodadas?status=PAUSADA");

        assertThat(resposta.status()).isEqualTo(400);
        assertThat(resposta.<String>json("$.codigo")).isEqualTo("STATUS_INEXISTENTE");
    }
}
