package dev.bluff.evento;

import dev.bluff.participante.ParticipanteRepository;
import dev.bluff.participante.ParticipanteResposta;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Locale;
import java.util.UUID;

/**
 * Traduz conexões STOMP em presença na sala.
 *
 * <p>O front envia dois cabeçalhos no frame CONNECT: {@code codigoSala} e {@code participanteId}.
 * Conexões sem eles (ou com um participante que não é daquela sala) funcionam normalmente
 * para receber eventos, mas não contam como presença.
 */
@Component
public class OuvinteDeConexoes {

    private final Presenca presenca;
    private final ParticipanteRepository participanteRepository;
    private final ApplicationEventPublisher eventos;

    public OuvinteDeConexoes(Presenca presenca,
                             ParticipanteRepository participanteRepository,
                             ApplicationEventPublisher eventos) {
        this.presenca = presenca;
        this.participanteRepository = participanteRepository;
        this.eventos = eventos;
    }

    @EventListener
    @Transactional(readOnly = true)
    public void aoConectar(SessionConnectEvent conexao) {
        StompHeaderAccessor cabecalhos = StompHeaderAccessor.wrap(conexao.getMessage());
        String codigoSala = cabecalhos.getFirstNativeHeader("codigoSala");
        UUID participanteId = lerUuid(cabecalhos.getFirstNativeHeader("participanteId"));
        if (codigoSala == null || participanteId == null) {
            return;
        }
        String codigo = codigoSala.strip().toUpperCase(Locale.ROOT);

        participanteRepository.findById(participanteId)
                .filter(participante -> participante.getSala().getCodigo().equals(codigo))
                .ifPresent(participante -> {
                    boolean acabouDeFicarOnline = presenca.conectar(
                            cabecalhos.getSessionId(), new Presenca.Sessao(codigo, participanteId));
                    if (acabouDeFicarOnline) {
                        eventos.publishEvent(new EventoSala(codigo, TipoEvento.PARTICIPANTE_ENTROU,
                                ParticipanteResposta.de(participante, true)));
                    }
                });
    }

    @EventListener
    public void aoDesconectar(SessionDisconnectEvent desconexao) {
        presenca.desconectar(desconexao.getSessionId()).ifPresent(sessao ->
                eventos.publishEvent(new EventoSala(sessao.codigoSala(), TipoEvento.PARTICIPANTE_SAIU,
                        new EventoSala.ParticipanteSaiu(sessao.participanteId()))));
    }

    private static UUID lerUuid(String texto) {
        try {
            return texto == null ? null : UUID.fromString(texto);
        } catch (IllegalArgumentException invalido) {
            return null;
        }
    }
}
