package dev.bluff.sala;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Gera o código que as pessoas digitam para entrar numa sala (ex.: "K7PX3M").
 *
 * <p>O alfabeto não tem 0, O, 1, I nem L, que se confundem quando o código é lido em voz alta
 * ou digitado. São 31 símbolos em 6 posições: cerca de 887 milhões de combinações.
 * Usamos {@link SecureRandom} para que os códigos não sejam previsíveis a partir dos anteriores.
 *
 * <p>Esta classe não garante unicidade: quem garante é a constraint {@code uk_sala_codigo},
 * e o {@link SalaService} tenta de novo em caso de colisão.
 */
@Component
public class GeradorCodigoSala {

    static final String ALFABETO = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    static final int TAMANHO = 6;

    private final SecureRandom aleatorio = new SecureRandom();

    public String gerar() {
        StringBuilder codigo = new StringBuilder(TAMANHO);
        for (int i = 0; i < TAMANHO; i++) {
            codigo.append(ALFABETO.charAt(aleatorio.nextInt(ALFABETO.length())));
        }
        return codigo.toString();
    }
}
