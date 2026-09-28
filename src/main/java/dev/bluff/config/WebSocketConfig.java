package dev.bluff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP sobre WebSocket.
 *
 * <ul>
 *   <li>O cliente abre a conexão em {@code /ws}.</li>
 *   <li>Assina {@code /topic/salas/{codigo}} para receber os eventos da sala.</li>
 *   <li>O back publica nesse tópico; o broker simples (em memória) entrega a todos os assinantes.</li>
 * </ul>
 *
 * Os clientes só escutam: todas as ações (votar, revelar...) vão pela API REST.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /** Intervalo dos heartbeats. Sem eles, uma aba fechada à força demoraria a ser notada. */
    private static final long HEARTBEAT_MS = 10_000;

    private final String[] origens;

    public WebSocketConfig(@Value("${bluff.cors.origens}") String[] origens) {
        this.origens = origens;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins(origens);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        ThreadPoolTaskScheduler agendador = new ThreadPoolTaskScheduler();
        agendador.setPoolSize(1);
        agendador.setThreadNamePrefix("ws-heartbeat-");
        agendador.initialize();

        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[] {HEARTBEAT_MS, HEARTBEAT_MS})
                .setTaskScheduler(agendador);
        registry.setApplicationDestinationPrefixes("/app");
    }
}
