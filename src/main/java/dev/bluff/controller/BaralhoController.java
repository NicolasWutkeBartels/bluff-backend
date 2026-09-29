package dev.bluff.controller;

import dev.bluff.dto.baralho.BaralhoResponse;
import dev.bluff.model.enums.Baralho;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/baralhos")
public class BaralhoController {

    @GetMapping
    public List<BaralhoResponse> listar() {
        return Arrays.stream(Baralho.values()).map(BaralhoResponse::de).toList();
    }

    @GetMapping("/{codigo}/cartas")
    public List<String> listarCartas(@PathVariable String codigo) {
        return Baralho.porCodigo(codigo).getCartas();
    }
}
