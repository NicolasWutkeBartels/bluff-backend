package dev.bluff.sala;

import java.util.List;

public record BaralhoResposta(String codigo, String descricao, List<String> cartas) {

    public static BaralhoResposta de(Baralho baralho) {
        return new BaralhoResposta(baralho.name(), baralho.getDescricao(), baralho.getCartas());
    }
}
