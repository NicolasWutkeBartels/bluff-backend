package dev.bluff.sala;

import java.util.List;

/**
 * Baralhos disponíveis. O banco guarda só o nome da constante (sal_baralho);
 * as cartas ficam aqui para que o front e a validação do voto usem a mesma fonte.
 */
public enum Baralho {

    FIBONACCI("Fibonacci", List.of("1", "2", "3", "5", "8", "13", "21")),
    CAMISETA("Camiseta", List.of("PP", "P", "M", "G", "GG"));

    private final String descricao;
    private final List<String> cartas;

    Baralho(String descricao, List<String> cartas) {
        this.descricao = descricao;
        this.cartas = cartas;
    }

    public boolean temCarta(String valor) {
        return cartas.contains(valor);
    }

    public String getDescricao() { return descricao; }
    public List<String> getCartas() { return cartas; }
}
