package dev.bluff.sala;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controllers só traduzem HTTP para chamadas de service: leem o caminho, o corpo e os cabeçalhos,
 * e escolhem o status da resposta. Nenhuma regra de negócio mora aqui.
 */
@RestController
@RequestMapping("/salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalaRespostas.Entrada criar(@Valid @RequestBody SalaRequisicoes.CriarSala requisicao) {
        return salaService.criar(requisicao);
    }

    @PostMapping("/{codigo}/participantes")
    @ResponseStatus(HttpStatus.CREATED)
    public SalaRespostas.Entrada entrar(@PathVariable String codigo,
                                        @Valid @RequestBody SalaRequisicoes.EntrarNaSala requisicao) {
        return salaService.entrar(codigo, requisicao);
    }

    @GetMapping("/{codigo}")
    public SalaRespostas.Estado estado(@PathVariable String codigo) {
        return salaService.estado(codigo);
    }
}
