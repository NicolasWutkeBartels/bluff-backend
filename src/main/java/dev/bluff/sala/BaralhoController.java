package dev.bluff.sala;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/** Os baralhos são fixos no código (enum), então não há service nem repository. */
@RestController
@RequestMapping("/baralhos")
public class BaralhoController {

    @GetMapping
    public List<BaralhoResposta> listar() {
        return Arrays.stream(Baralho.values()).map(BaralhoResposta::de).toList();
    }

    @GetMapping("/{codigo}/cartas")
    public List<String> cartas(@PathVariable String codigo) {
        return Baralho.porCodigo(codigo).getCartas();
    }
}
