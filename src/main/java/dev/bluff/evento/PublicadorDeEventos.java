package dev.bluff.evento;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envia os eventos das salas para o broker STOMP.
 *
 * <p>{@code AFTER_COMMIT}: o evento só sai depois que a transação que o gerou foi gravada.
 * Se saísse antes, um cliente poderia reagir ao evento buscando o estado da sala e
 * ainda não encontrar o dado novo (ou encontrar um dado que depois sofreu rollback).
 * {@code fallbackExecution}: eventos publicados fora de transação (presença) saem na hora.
 */
@Component
public class PublicadorDeEventos {

    private final SimpMessagingTemplate mensageiro;

    public PublicadorDeEventos(SimpMessagingTemplate mensageiro) {
        this.mensageiro = mensageiro;
    }

    public static String topico(String codigoSala) {
        return "/topic/salas/" + codigoSala;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publicar(EventoSala evento) {
        mensageiro.convertAndSend(topico(evento.codigoSala()), new EventoSala.Mensagem(evento.tipo(), evento.dados()));
    }
}
