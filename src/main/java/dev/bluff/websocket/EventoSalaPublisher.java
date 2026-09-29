package dev.bluff.websocket;

import dev.bluff.dto.evento.EventoMensagem;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EventoSalaPublisher {

    private final SimpMessagingTemplate mensageiro;

    public EventoSalaPublisher(SimpMessagingTemplate mensageiro) {
        this.mensageiro = mensageiro;
    }

    public static String topico(String codigoSala) {
        return "/topic/salas/" + codigoSala;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publicar(EventoSala evento) {
        mensageiro.convertAndSend(topico(evento.codigoSala()), new EventoMensagem(evento.tipo(), evento.dados()));
    }
}
