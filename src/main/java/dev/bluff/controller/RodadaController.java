package dev.bluff.controller;

import dev.bluff.dto.rodada.IniciarRodadaRequest;
import dev.bluff.dto.rodada.RevelarRodadaRequest;
import dev.bluff.dto.rodada.RodadaResponse;
import dev.bluff.dto.voto.VotarRequest;
import dev.bluff.service.RodadaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class RodadaController {

    private final RodadaService rodadaService;

    public RodadaController(RodadaService rodadaService) {
        this.rodadaService = rodadaService;
    }

    @PostMapping("/salas/{codigo}/rodadas")
    @ResponseStatus(HttpStatus.CREATED)
    public RodadaResponse iniciar(@PathVariable String codigo,
                                  @RequestHeader(ApiHeaders.PARTICIPANTE_ID) UUID participanteId,
                                  @Valid @RequestBody IniciarRodadaRequest requisicao) {
        return rodadaService.iniciar(codigo, participanteId, requisicao);
    }

    @GetMapping("/salas/{codigo}/rodadas")
    public List<RodadaResponse> listarHistorico(@PathVariable String codigo,
                                                @RequestParam(required = false) String status) {
        return rodadaService.listarHistorico(codigo, status);
    }

    @PostMapping("/rodadas/{id}/votos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void votar(@PathVariable UUID id,
                      @RequestHeader(ApiHeaders.PARTICIPANTE_ID) UUID participanteId,
                      @Valid @RequestBody VotarRequest requisicao) {
        rodadaService.votar(id, participanteId, requisicao);
    }

    @PostMapping("/rodadas/{id}/revelar")
    public RodadaResponse revelar(@PathVariable UUID id,
                                  @RequestHeader(ApiHeaders.PARTICIPANTE_ID) UUID participanteId,
                                  @Valid @RequestBody(required = false) RevelarRodadaRequest requisicao) {
        return rodadaService.revelar(id, participanteId, requisicao);
    }
}
