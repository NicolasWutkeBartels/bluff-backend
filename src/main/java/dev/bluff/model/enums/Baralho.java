package dev.bluff.model.enums;

import dev.bluff.exception.NegocioException;
import dev.bluff.exception.TipoErro;

import java.util.Arrays;
import java.util.List;

public enum Baralho {

    FIBONACCI("Fibonacci", List.of("1", "2", "3", "5", "8", "13", "21")),
    CAMISETA("Camiseta", List.of("PP", "P", "M", "G", "GG"));

    private final String descricao;
    private final List<String> cartas;

    Baralho(String descricao, List<String> cartas) {
        this.descricao = descricao;
        this.cartas = cartas;
    }

    public static Baralho porCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(baralho -> baralho.name().equalsIgnoreCase(codigo))
                .findFirst()
                .orElseThrow(() -> new NegocioException(TipoErro.BARALHO_INEXISTENTE, codigo));
    }

    public boolean temCarta(String valor) {
        return cartas.contains(valor);
    }

    public String getDescricao() { return descricao; }
    public List<String> getCartas() { return cartas; }
}
