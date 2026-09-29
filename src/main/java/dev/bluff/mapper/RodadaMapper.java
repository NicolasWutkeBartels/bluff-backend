package dev.bluff.mapper;

import dev.bluff.dto.dominio.DominioResponse;
import dev.bluff.dto.rodada.RodadaResponse;
import dev.bluff.dto.voto.VotoResponse;
import dev.bluff.model.Rodada;
import dev.bluff.model.Voto;
import dev.bluff.repository.VotoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RodadaMapper {

    private final VotoRepository votoRepository;

    public RodadaMapper(VotoRepository votoRepository) {
        this.votoRepository = votoRepository;
    }

    public RodadaResponse montar(Rodada rodada) {
        return montar(List.of(rodada)).getFirst();
    }

    public List<RodadaResponse> montar(List<Rodada> rodadas) {
        if (rodadas.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<Voto>> votosPorRodada = votoRepository.buscarPorRodadas(rodadas).stream()
                .collect(Collectors.groupingBy(voto -> voto.getRodada().getId()));

        return rodadas.stream()
                .map(rodada -> montar(rodada, votosPorRodada.getOrDefault(rodada.getId(), List.of())))
                .toList();
    }

    private RodadaResponse montar(Rodada rodada, List<Voto> votos) {
        List<VotoResponse> votosVisiveis = votos.stream()
                .map(voto -> rodada.foiRevelada() ? VotoResponse.revelado(voto) : VotoResponse.oculto(voto))
                .toList();

        return new RodadaResponse(
                rodada.getId(),
                rodada.getTitulo(),
                DominioResponse.de(rodada.getStatus()),
                rodada.getEstimativaFinal(),
                rodada.getCriadoEm(),
                votosVisiveis);
    }
}
