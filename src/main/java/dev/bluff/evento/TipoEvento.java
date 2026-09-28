package dev.bluff.evento;

/** Eventos enviados em /topic/salas/{codigo}. */
public enum TipoEvento {
    /** Alguém entrou na sala ou voltou a ficar online. Dados: participante. */
    PARTICIPANTE_ENTROU,
    /** A última conexão de um participante caiu. Dados: participanteId. */
    PARTICIPANTE_SAIU,
    /** Nova rodada em votação. Dados: rodada (sem votos). */
    RODADA_INICIADA,
    /** Alguém votou ou trocou de carta. Dados: rodadaId e participanteId, <strong>nunca o valor</strong>. */
    VOTO_REGISTRADO,
    /** Votos abertos. Dados: rodada com todos os valores. */
    RODADA_REVELADA
}
