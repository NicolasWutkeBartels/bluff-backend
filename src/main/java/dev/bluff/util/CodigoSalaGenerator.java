package dev.bluff.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CodigoSalaGenerator {

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
