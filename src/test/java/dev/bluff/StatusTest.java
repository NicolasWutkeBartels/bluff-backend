package dev.bluff;

import dev.bluff.erro.ErroNegocio;
import dev.bluff.erro.TipoErro;
import dev.bluff.status.StatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusTest extends TesteDeIntegracao {

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
    void buscarStatusInexistenteLancaErroDeNegocio() {
        assertThatThrownBy(() -> statusService.buscarStatus("RODADA", "PAUSADA"))
                .isInstanceOf(ErroNegocio.class)
                .extracting(erro -> ((ErroNegocio) erro).getTipo())
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
