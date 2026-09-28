package dev.bluff.rodada;

import dev.bluff.erro.Constraints;
import dev.bluff.erro.ErroNegocio;
import dev.bluff.erro.TipoErro;
import dev.bluff.participante.Participante;
import dev.bluff.sala.Baralho;
import dev.bluff.sala.Sala;
import dev.bluff.sala.SalaService;
import dev.bluff.status.Status;
import dev.bluff.status.StatusRodada;
import dev.bluff.status.StatusService;
import dev.bluff.voto.Voto;
import dev.bluff.voto.VotoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Regras das rodadas. Todas as verificações de negócio (quem pode votar, em que estado,
 * com que carta) ficam aqui, e não no controller nem na entidade, para que qualquer
 * porta de entrada (REST hoje, talvez outra amanhã) passe pelas mesmas regras.
 */
@Service
public class RodadaService {

    private final RodadaRepository rodadaRepository;
    private final VotoRepository votoRepository;
    private final SalaService salaService;
    private final StatusService statusService;
    private final MontadorDeRodada montadorDeRodada;

    public RodadaService(RodadaRepository rodadaRepository,
                         VotoRepository votoRepository,
                         SalaService salaService,
                         StatusService statusService,
                         MontadorDeRodada montadorDeRodada) {
        this.rodadaRepository = rodadaRepository;
        this.votoRepository = votoRepository;
        this.salaService = salaService;
        this.statusService = statusService;
        this.montadorDeRodada = montadorDeRodada;
    }

    @Transactional
    public RodadaResposta iniciar(String codigoSala, UUID participanteId, RodadaRequisicoes.Iniciar requisicao) {
        // A trava na sala faz dois pedidos simultâneos passarem um de cada vez pela checagem abaixo.
        Sala sala = salaService.buscarSalaComTrava(codigoSala);
        salaService.buscarParticipante(sala, participanteId);

        Status emVotacao = statusService.buscarStatus(StatusRodada.DOMINIO, StatusRodada.VOTING);
        if (rodadaRepository.existsBySalaAndStatus(sala, emVotacao)) {
            throw new ErroNegocio(TipoErro.RODADA_EM_ANDAMENTO);
        }

        Rodada rodada = rodadaRepository.save(new Rodada(sala, emVotacao, requisicao.titulo().strip()));
        return montadorDeRodada.montar(rodada);
    }

    /** Registra o voto ou, se a pessoa já votou nesta rodada, troca a carta. */
    @Transactional
    public void votar(UUID rodadaId, UUID participanteId, RodadaRequisicoes.Votar requisicao) {
        Rodada rodada = buscarRodadaComTrava(rodadaId);
        Participante participante = salaService.buscarParticipante(rodada.getSala(), participanteId);

        if (!participante.podeVotar()) {
            throw new ErroNegocio(TipoErro.OBSERVADOR_NAO_VOTA);
        }
        if (!rodada.estaEmVotacao()) {
            throw new ErroNegocio(TipoErro.RODADA_FECHADA_PARA_VOTOS);
        }
        String valor = validarCarta(rodada.getSala().getBaralho(), requisicao.valor());

        votoRepository.findByRodadaAndParticipante(rodada, participante)
                .ifPresentOrElse(
                        voto -> voto.trocarValor(valor),
                        () -> gravarVotoNovo(rodada, participante, valor));
    }

    @Transactional
    public RodadaResposta revelar(UUID rodadaId, UUID participanteId, RodadaRequisicoes.Revelar requisicao) {
        Rodada rodada = buscarRodadaComTrava(rodadaId);
        salaService.buscarParticipante(rodada.getSala(), participanteId);

        if (!rodada.estaEmVotacao()) {
            throw new ErroNegocio(TipoErro.RODADA_JA_REVELADA);
        }
        String estimativa = requisicao == null || requisicao.estimativaFinal() == null
                || requisicao.estimativaFinal().isBlank()
                ? null
                : validarCarta(rodada.getSala().getBaralho(), requisicao.estimativaFinal());

        rodada.revelar(statusService.buscarStatus(StatusRodada.DOMINIO, StatusRodada.REVEALED), estimativa);
        return montadorDeRodada.montar(rodada);
    }

    /**
     * Histórico da sala, da rodada mais nova para a mais antiga.
     * O filtro recebe o código do status (ex.: REVEALED), nunca o id.
     */
    @Transactional(readOnly = true)
    public List<RodadaResposta> historico(String codigoSala, String codigoStatus) {
        Sala sala = salaService.buscarSala(codigoSala);
        List<Rodada> rodadas = codigoStatus == null || codigoStatus.isBlank()
                ? rodadaRepository.findBySalaOrderByCriadoEmDesc(sala)
                : rodadaRepository.findBySalaAndStatusOrderByCriadoEmDesc(
                        sala, statusService.buscarStatus(StatusRodada.DOMINIO, codigoStatus));
        return montadorDeRodada.montar(rodadas);
    }

    private void gravarVotoNovo(Rodada rodada, Participante participante, String valor) {
        try {
            votoRepository.saveAndFlush(new Voto(rodada, participante, valor));
        } catch (DataIntegrityViolationException erro) {
            // Dois cliques quase simultâneos: o banco aceitou o primeiro e barrou o segundo.
            if (Constraints.violou(erro, Constraints.VOTO_RODADA_PARTICIPANTE)) {
                throw new ErroNegocio(TipoErro.VOTO_SIMULTANEO);
            }
            throw erro;
        }
    }

    private Rodada buscarRodadaComTrava(UUID rodadaId) {
        return rodadaRepository.buscarComTrava(rodadaId)
                .orElseThrow(() -> new ErroNegocio(TipoErro.RODADA_NAO_ENCONTRADA));
    }

    private static String validarCarta(Baralho baralho, String valor) {
        String carta = valor.strip();
        if (!baralho.temCarta(carta)) {
            throw new ErroNegocio(TipoErro.CARTA_INVALIDA, carta, baralho.getDescricao());
        }
        return carta;
    }
}
