package dev.bluff.dto.baralho;

import dev.bluff.model.enums.Baralho;

import java.util.List;

public record BaralhoResponse(String codigo, String descricao, List<String> cartas) {

    public static BaralhoResponse de(Baralho baralho) {
        return new BaralhoResponse(baralho.name(), baralho.getDescricao(), baralho.getCartas());
    }
}
