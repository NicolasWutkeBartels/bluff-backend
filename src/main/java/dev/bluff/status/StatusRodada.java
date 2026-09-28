package dev.bluff.status;

/**
 * Códigos do domínio RODADA que o Java precisa conhecer para aplicar regras.
 * Os registros em si (e as descrições) vivem na tabela status.
 */
public final class StatusRodada {

    public static final String DOMINIO = "RODADA";
    public static final String VOTING = "VOTING";
    public static final String REVEALED = "REVEALED";

    private StatusRodada() {
    }
}
