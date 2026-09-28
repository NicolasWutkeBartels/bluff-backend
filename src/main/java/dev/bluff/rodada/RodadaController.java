package dev.bluff.rodada;

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

/**
 * Ações de rodada exigem o cabeçalho {@value #PARTICIPANTE}: é assim que, sem login,
 * o back sabe quem está agindo e se a pessoa pertence à sala.
 */
@RestController
public class RodadaController {

    public static final String PARTICIPANTE = "X-Participante-Id";

    private final RodadaService rodadaService;

    public RodadaController(RodadaService rodadaService) {
        this.rodadaService = rodadaService;
    }

    @PostMapping("/salas/{codigo}/rodadas")
    @ResponseStatus(HttpStatus.CREATED)
    public RodadaResposta iniciar(@PathVariable String codigo,
                                  @RequestHeader(PARTICIPANTE) UUID participanteId,
                                  @Valid @RequestBody RodadaRequisicoes.Iniciar requisicao) {
        return rodadaService.iniciar(codigo, participanteId, requisicao);
    }

    @GetMapping("/salas/{codigo}/rodadas")
    public List<RodadaResposta> historico(@PathVariable String codigo,
                                          @RequestParam(required = false) String status) {
        return rodadaService.historico(codigo, status);
    }

    /** Responde 204 sem corpo: nem o próprio voto volta enquanto a rodada está em votação. */
    @PostMapping("/rodadas/{id}/votos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void votar(@PathVariable UUID id,
                      @RequestHeader(PARTICIPANTE) UUID participanteId,
                      @Valid @RequestBody RodadaRequisicoes.Votar requisicao) {
        rodadaService.votar(id, participanteId, requisicao);
    }

    @PostMapping("/rodadas/{id}/revelar")
    public RodadaResposta revelar(@PathVariable UUID id,
                                  @RequestHeader(PARTICIPANTE) UUID participanteId,
                                  @Valid @RequestBody(required = false) RodadaRequisicoes.Revelar requisicao) {
        return rodadaService.revelar(id, participanteId, requisicao);
    }
}
