package dev.bluff.service;

import dev.bluff.dto.baralho.BaralhoResponse;
import dev.bluff.dto.evento.TipoEvento;
import dev.bluff.dto.participante.ParticipanteResponse;
import dev.bluff.dto.sala.CriarSalaRequest;
import dev.bluff.dto.sala.EntradaSalaResponse;
import dev.bluff.dto.sala.EntrarSalaRequest;
import dev.bluff.dto.sala.EstadoSalaResponse;
import dev.bluff.exception.Constraints;
import dev.bluff.exception.NegocioException;
import dev.bluff.exception.TipoErro;
import dev.bluff.mapper.RodadaMapper;
import dev.bluff.model.Participante;
import dev.bluff.model.Sala;
import dev.bluff.model.enums.Baralho;
import dev.bluff.model.enums.Papel;
import dev.bluff.repository.ParticipanteRepository;
import dev.bluff.repository.RodadaRepository;
import dev.bluff.repository.SalaRepository;
import dev.bluff.util.CodigoSalaGenerator;
import dev.bluff.websocket.EventoSala;
import dev.bluff.websocket.PresencaRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
public class SalaService {

    static final int MAXIMO_TENTATIVAS_CODIGO = 5;

    private final SalaRepository salaRepository;
    private final ParticipanteRepository participanteRepository;
    private final RodadaRepository rodadaRepository;
    private final RodadaMapper rodadaMapper;
    private final CodigoSalaGenerator codigoSalaGenerator;
    private final TransactionTemplate transacao;
    private final PresencaRegistry presenca;
    private final ApplicationEventPublisher eventos;

    public SalaService(SalaRepository salaRepository,
                       ParticipanteRepository participanteRepository,
                       RodadaRepository rodadaRepository,
                       RodadaMapper rodadaMapper,
                       CodigoSalaGenerator codigoSalaGenerator,
                       TransactionTemplate transacao,
                       PresencaRegistry presenca,
                       ApplicationEventPublisher eventos) {
        this.salaRepository = salaRepository;
        this.participanteRepository = participanteRepository;
        this.rodadaRepository = rodadaRepository;
        this.rodadaMapper = rodadaMapper;
        this.codigoSalaGenerator = codigoSalaGenerator;
        this.transacao = transacao;
        this.presenca = presenca;
        this.eventos = eventos;
    }

    public EntradaSalaResponse criar(CriarSalaRequest requisicao) {
        Baralho baralho = Objects.requireNonNullElse(requisicao.baralho(), Baralho.FIBONACCI);
        Papel papel = Objects.requireNonNullElse(requisicao.papel(), Papel.VOTANTE);

        for (int tentativa = 1; tentativa <= MAXIMO_TENTATIVAS_CODIGO; tentativa++) {
            String codigo = codigoSalaGenerator.gerar();
            try {
                return transacao.execute(status -> {
                    Sala sala = salaRepository.saveAndFlush(new Sala(codigo, requisicao.nomeSala().strip(), baralho));
                    Participante criador = participanteRepository.save(
                            new Participante(sala, requisicao.apelido().strip(), papel));
                    return new EntradaSalaResponse(sala.getCodigo(), ParticipanteResponse.de(criador, false));
                });
            } catch (DataIntegrityViolationException erro) {
                if (!Constraints.violou(erro, Constraints.SALA_CODIGO)) {
                    throw erro;
                }
            }
        }
        throw new NegocioException(TipoErro.CODIGO_SALA_INDISPONIVEL);
    }

    @Transactional
    public EntradaSalaResponse entrar(String codigoSala, EntrarSalaRequest requisicao) {
        Sala sala = buscarSala(codigoSala);
        String apelido = requisicao.apelido().strip();
        Papel papel = Objects.requireNonNullElse(requisicao.papel(), Papel.VOTANTE);
        try {
            Participante participante = participanteRepository.saveAndFlush(new Participante(sala, apelido, papel));
            ParticipanteResponse resposta = ParticipanteResponse.de(participante, false);
            eventos.publishEvent(new EventoSala(sala.getCodigo(), TipoEvento.PARTICIPANTE_ENTROU, resposta));
            return new EntradaSalaResponse(sala.getCodigo(), resposta);
        } catch (DataIntegrityViolationException erro) {
            if (Constraints.violou(erro, Constraints.PARTICIPANTE_APELIDO)) {
                throw new NegocioException(TipoErro.APELIDO_EM_USO, apelido);
            }
            throw erro;
        }
    }

    @Transactional(readOnly = true)
    public EstadoSalaResponse buscarEstado(String codigoSala) {
        Sala sala = buscarSala(codigoSala);
        var participantes = participanteRepository.findBySalaOrderByCriadoEm(sala).stream()
                .map(participante -> ParticipanteResponse.de(participante, presenca.estaOnline(participante.getId())))
                .toList();
        var rodadaAtual = rodadaRepository.findFirstBySalaOrderByCriadoEmDesc(sala)
                .map(rodadaMapper::montar)
                .orElse(null);
        return new EstadoSalaResponse(
                sala.getCodigo(), sala.getNome(), BaralhoResponse.de(sala.getBaralho()), participantes, rodadaAtual);
    }

    public Sala buscarSala(String codigo) {
        return salaRepository.findByCodigo(normalizar(codigo))
                .orElseThrow(() -> new NegocioException(TipoErro.SALA_NAO_ENCONTRADA, codigo));
    }

    public Sala buscarSalaComTrava(String codigo) {
        return salaRepository.buscarComTrava(normalizar(codigo))
                .orElseThrow(() -> new NegocioException(TipoErro.SALA_NAO_ENCONTRADA, codigo));
    }

    public Participante buscarParticipante(Sala sala, UUID participanteId) {
        return participanteRepository.findByIdAndSala(participanteId, sala)
                .orElseThrow(() -> new NegocioException(TipoErro.PARTICIPANTE_FORA_DA_SALA));
    }

    private static String normalizar(String codigo) {
        return codigo.strip().toUpperCase(Locale.ROOT);
    }
}
