package dev.bluff.rodada;

import dev.bluff.status.DominioResposta;
import dev.bluff.voto.Voto;
import dev.bluff.voto.VotoRepository;
import dev.bluff.voto.VotoResposta;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Transforma rodadas em {@link RodadaResposta}. É o <strong>único</strong> lugar que decide se o valor
 * de um voto pode sair do servidor.
 *
 * <p>Regra dos votos ocultos: enquanto a rodada está em VOTING, nenhuma resposta (REST ou WebSocket)
 * pode conter o valor de ninguém, nem o da própria pessoa. Só em REVEALED os valores aparecem.
 * Como toda resposta de rodada passa por aqui (estado da sala, histórico, eventos),
 * a regra não depende de cada endpoint lembrar de escondê-los.
 */
@Component
public class MontadorDeRodada {

    private final VotoRepository votoRepository;

    public MontadorDeRodada(VotoRepository votoRepository) {
        this.votoRepository = votoRepository;
    }

    public RodadaResposta montar(Rodada rodada) {
        return montar(List.of(rodada)).getFirst();
    }

    /** Monta várias rodadas buscando os votos de todas numa só consulta. */
    public List<RodadaResposta> montar(List<Rodada> rodadas) {
        if (rodadas.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<Voto>> votosPorRodada = votoRepository.buscarPorRodadas(rodadas).stream()
                .collect(Collectors.groupingBy(voto -> voto.getRodada().getId()));

        return rodadas.stream()
                .map(rodada -> montar(rodada, votosPorRodada.getOrDefault(rodada.getId(), List.of())))
                .toList();
    }

    private RodadaResposta montar(Rodada rodada, List<Voto> votos) {
        List<VotoResposta> votosVisiveis = votos.stream()
                .map(voto -> rodada.foiRevelada() ? VotoResposta.revelado(voto) : VotoResposta.oculto(voto))
                .toList();

        return new RodadaResposta(
                rodada.getId(),
                rodada.getTitulo(),
                DominioResposta.de(rodada.getStatus()),
                rodada.getEstimativaFinal(),
                rodada.getCriadoEm(),
                votosVisiveis);
    }
}
