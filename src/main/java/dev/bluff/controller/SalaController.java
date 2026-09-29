package dev.bluff.controller;

import dev.bluff.dto.sala.CriarSalaRequest;
import dev.bluff.dto.sala.EntradaSalaResponse;
import dev.bluff.dto.sala.EntrarSalaRequest;
import dev.bluff.dto.sala.EstadoSalaResponse;
import dev.bluff.service.SalaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntradaSalaResponse criar(@Valid @RequestBody CriarSalaRequest requisicao) {
        return salaService.criar(requisicao);
    }

    @PostMapping("/{codigo}/participantes")
    @ResponseStatus(HttpStatus.CREATED)
    public EntradaSalaResponse entrar(@PathVariable String codigo,
                                      @Valid @RequestBody EntrarSalaRequest requisicao) {
        return salaService.entrar(codigo, requisicao);
    }

    @GetMapping("/{codigo}")
    public EstadoSalaResponse buscarEstado(@PathVariable String codigo) {
        return salaService.buscarEstado(codigo);
    }
}
