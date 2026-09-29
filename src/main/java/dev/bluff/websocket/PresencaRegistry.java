package dev.bluff.websocket;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PresencaRegistry {

    public record Sessao(String codigoSala, UUID participanteId) {
    }

    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> conexoesPorParticipante = new ConcurrentHashMap<>();

    public boolean conectar(String sessaoId, Sessao sessao) {
        sessoes.put(sessaoId, sessao);
        return conexoesPorParticipante.merge(sessao.participanteId(), 1, Integer::sum) == 1;
    }

    public Optional<Sessao> desconectar(String sessaoId) {
        Sessao sessao = sessoes.remove(sessaoId);
        if (sessao == null) {
            return Optional.empty();
        }
        Integer restantes = conexoesPorParticipante.computeIfPresent(
                sessao.participanteId(), (id, total) -> total > 1 ? total - 1 : null);
        return restantes == null ? Optional.of(sessao) : Optional.empty();
    }

    public boolean estaOnline(UUID participanteId) {
        return conexoesPorParticipante.containsKey(participanteId);
    }
}
