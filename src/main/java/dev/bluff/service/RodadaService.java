package dev.bluff.service;

import dev.bluff.dto.evento.TipoEvento;
import dev.bluff.dto.evento.VotoRegistradoEvento;
import dev.bluff.dto.rodada.IniciarRodadaRequest;
import dev.bluff.dto.rodada.RevelarRodadaRequest;
import dev.bluff.dto.rodada.RodadaResponse;
import dev.bluff.dto.voto.VotarRequest;
import dev.bluff.exception.Constraints;
import dev.bluff.exception.NegocioException;
import dev.bluff.exception.TipoErro;
import dev.bluff.mapper.RodadaMapper;
import dev.bluff.model.Participante;
import dev.bluff.model.Rodada;
import dev.bluff.model.Sala;
import dev.bluff.model.Status;
import dev.bluff.model.StatusRodada;
import dev.bluff.model.Voto;
import dev.bluff.model.enums.Baralho;
import dev.bluff.repository.RodadaRepository;
import dev.bluff.repository.VotoRepository;
import dev.bluff.websocket.EventoSala;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RodadaService {

    private final RodadaRepository rodadaRepository;
    private final VotoRepository votoRepository;
    private final SalaService salaService;
    private final StatusService statusService;
    private final RodadaMapper rodadaMapper;
    private final ApplicationEventPublisher eventos;

    public RodadaService(RodadaRepository rodadaRepository,
                         VotoRepository votoRepository,
                         SalaService salaService,
                         StatusService statusService,
                         RodadaMapper rodadaMapper,
                         ApplicationEventPublisher eventos) {
        this.rodadaRepository = rodadaRepository;
        this.votoRepository = votoRepository;
        this.salaService = salaService;
        this.statusService = statusService;
        this.rodadaMapper = rodadaMapper;
        this.eventos = eventos;
    }

    @Transactional
    public RodadaResponse iniciar(String codigoSala, UUID participanteId, IniciarRodadaRequest requisicao) {
        Sala sala = salaService.buscarSalaComTrava(codigoSala);
        salaService.buscarParticipante(sala, participanteId);

        Status emVotacao = statusService.buscarPorCodigo(StatusRodada.DOMINIO, StatusRodada.VOTING);
        if (rodadaRepository.existsBySalaAndStatus(sala, emVotacao)) {
            throw new NegocioException(TipoErro.RODADA_EM_ANDAMENTO);
        }

        Rodada rodada = rodadaRepository.save(new Rodada(sala, emVotacao, requisicao.titulo().strip()));
        RodadaResponse resposta = rodadaMapper.montar(rodada);
        eventos.publishEvent(new EventoSala(sala.getCodigo(), TipoEvento.RODADA_INICIADA, resposta));
        return resposta;
    }

    @Transactional
    public void votar(UUID rodadaId, UUID participanteId, VotarRequest requisicao) {
        Rodada rodada = buscarRodadaComTrava(rodadaId);
        Participante participante = salaService.buscarParticipante(rodada.getSala(), participanteId);

        if (!participante.podeVotar()) {
            throw new NegocioException(TipoErro.OBSERVADOR_NAO_VOTA);
        }
        if (!rodada.estaEmVotacao()) {
            throw new NegocioException(TipoErro.RODADA_FECHADA_PARA_VOTOS);
        }
        String valor = validarCarta(rodada.getSala().getBaralho(), requisicao.valor());

        votoRepository.findByRodadaAndParticipante(rodada, participante)
                .ifPresentOrElse(
                        voto -> voto.trocarValor(valor),
                        () -> gravarVotoNovo(rodada, participante, valor));

        eventos.publishEvent(new EventoSala(rodada.getSala().getCodigo(), TipoEvento.VOTO_REGISTRADO,
                new VotoRegistradoEvento(rodada.getId(), participante.getId())));
    }

    @Transactional
    public RodadaResponse revelar(UUID rodadaId, UUID participanteId, RevelarRodadaRequest requisicao) {
        Rodada rodada = buscarRodadaComTrava(rodadaId);
        salaService.buscarParticipante(rodada.getSala(), participanteId);

        if (!rodada.estaEmVotacao()) {
            throw new NegocioException(TipoErro.RODADA_JA_REVELADA);
        }
        String estimativa = requisicao == null || requisicao.estimativaFinal() == null
                || requisicao.estimativaFinal().isBlank()
                ? null
                : validarCarta(rodada.getSala().getBaralho(), requisicao.estimativaFinal());

        rodada.revelar(statusService.buscarPorCodigo(StatusRodada.DOMINIO, StatusRodada.REVEALED), estimativa);
        RodadaResponse resposta = rodadaMapper.montar(rodada);
        eventos.publishEvent(new EventoSala(rodada.getSala().getCodigo(), TipoEvento.RODADA_REVELADA, resposta));
        return resposta;
    }

    @Transactional(readOnly = true)
    public List<RodadaResponse> listarHistorico(String codigoSala, String codigoStatus) {
        Sala sala = salaService.buscarSala(codigoSala);
        List<Rodada> rodadas = codigoStatus == null || codigoStatus.isBlank()
                ? rodadaRepository.findBySalaOrderByCriadoEmDesc(sala)
                : rodadaRepository.findBySalaAndStatusOrderByCriadoEmDesc(
                        sala, statusService.buscarPorCodigo(StatusRodada.DOMINIO, codigoStatus));
        return rodadaMapper.montar(rodadas);
    }

    private void gravarVotoNovo(Rodada rodada, Participante participante, String valor) {
        try {
            votoRepository.saveAndFlush(new Voto(rodada, participante, valor));
        } catch (DataIntegrityViolationException erro) {
            if (Constraints.violou(erro, Constraints.VOTO_RODADA_PARTICIPANTE)) {
                throw new NegocioException(TipoErro.VOTO_SIMULTANEO);
            }
            throw erro;
        }
    }

    private Rodada buscarRodadaComTrava(UUID rodadaId) {
        return rodadaRepository.buscarComTrava(rodadaId)
                .orElseThrow(() -> new NegocioException(TipoErro.RODADA_NAO_ENCONTRADA));
    }

    private static String validarCarta(Baralho baralho, String valor) {
        String carta = valor.strip();
        if (!baralho.temCarta(carta)) {
            throw new NegocioException(TipoErro.CARTA_INVALIDA, carta, baralho.getDescricao());
        }
        return carta;
    }
}
