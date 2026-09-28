package dev.bluff.evento;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quem está conectado agora, em memória.
 *
 * <p>Uma pessoa pode ter várias conexões (duas abas, ou uma reconexão que chegou antes da
 * queda da anterior ser detectada). Por isso contamos sessões por participante: ele só
 * "sai" quando a última sessão fecha.
 *
 * <p>Limite conhecido: como fica em memória, só funciona com uma instância do back.
 * Com várias, a presença teria de ir para um lugar compartilhado (Redis, por exemplo).
 */
@Component
public class Presenca {

    public record Sessao(String codigoSala, UUID participanteId) {
    }

    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> conexoesPorParticipante = new ConcurrentHashMap<>();

    /** @return true se esta é a primeira conexão do participante (ele acabou de ficar online). */
    public boolean conectar(String sessaoId, Sessao sessao) {
        sessoes.put(sessaoId, sessao);
        return conexoesPorParticipante.merge(sessao.participanteId(), 1, Integer::sum) == 1;
    }

    /** @return a sessão encerrada, se esta era a última conexão do participante. */
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
