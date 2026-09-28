package dev.bluff.sala;

import dev.bluff.erro.Constraints;
import dev.bluff.erro.ErroNegocio;
import dev.bluff.erro.TipoErro;
import dev.bluff.participante.Papel;
import dev.bluff.participante.Participante;
import dev.bluff.participante.ParticipanteRepository;
import dev.bluff.participante.ParticipanteResposta;
import dev.bluff.rodada.MontadorDeRodada;
import dev.bluff.rodada.RodadaRepository;
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
    private final MontadorDeRodada montadorDeRodada;
    private final GeradorCodigoSala geradorCodigo;
    private final TransactionTemplate transacao;

    public SalaService(SalaRepository salaRepository,
                       ParticipanteRepository participanteRepository,
                       RodadaRepository rodadaRepository,
                       MontadorDeRodada montadorDeRodada,
                       GeradorCodigoSala geradorCodigo,
                       TransactionTemplate transacao) {
        this.salaRepository = salaRepository;
        this.participanteRepository = participanteRepository;
        this.rodadaRepository = rodadaRepository;
        this.montadorDeRodada = montadorDeRodada;
        this.geradorCodigo = geradorCodigo;
        this.transacao = transacao;
    }

    /**
     * Cria a sala e o primeiro participante.
     *
     * <p>Não checamos antes se o código já existe (outra requisição poderia criá-lo entre a checagem
     * e o INSERT). Em vez disso, tentamos gravar: se a constraint {@code uk_sala_codigo} reclamar,
     * geramos outro código e tentamos de novo, até 5 vezes.
     *
     * <p>Cada tentativa roda na sua própria transação ({@link TransactionTemplate}), porque depois
     * que o banco rejeita um INSERT a transação fica inutilizável e precisa ser descartada.
     * Por isso este método não é {@code @Transactional}.
     */
    public SalaRespostas.Entrada criar(SalaRequisicoes.CriarSala requisicao) {
        Baralho baralho = Objects.requireNonNullElse(requisicao.baralho(), Baralho.FIBONACCI);
        Papel papel = Objects.requireNonNullElse(requisicao.papel(), Papel.VOTANTE);

        for (int tentativa = 1; tentativa <= MAXIMO_TENTATIVAS_CODIGO; tentativa++) {
            String codigo = geradorCodigo.gerar();
            try {
                return transacao.execute(status -> {
                    Sala sala = salaRepository.saveAndFlush(new Sala(codigo, requisicao.nomeSala().strip(), baralho));
                    Participante criador = participanteRepository.save(
                            new Participante(sala, requisicao.apelido().strip(), papel));
                    return new SalaRespostas.Entrada(sala.getCodigo(), ParticipanteResposta.de(criador));
                });
            } catch (DataIntegrityViolationException erro) {
                if (!Constraints.violou(erro, Constraints.SALA_CODIGO)) {
                    throw erro;
                }
                // Colisão de código: segue para a próxima tentativa com um código novo.
            }
        }
        throw new ErroNegocio(TipoErro.CODIGO_SALA_INDISPONIVEL);
    }

    @Transactional
    public SalaRespostas.Entrada entrar(String codigoSala, SalaRequisicoes.EntrarNaSala requisicao) {
        Sala sala = buscarSala(codigoSala);
        String apelido = requisicao.apelido().strip();
        Papel papel = Objects.requireNonNullElse(requisicao.papel(), Papel.VOTANTE);
        try {
            // A unicidade do apelido é garantida pelo banco; aqui só traduzimos o erro.
            Participante participante = participanteRepository.saveAndFlush(new Participante(sala, apelido, papel));
            return new SalaRespostas.Entrada(sala.getCodigo(), ParticipanteResposta.de(participante));
        } catch (DataIntegrityViolationException erro) {
            if (Constraints.violou(erro, Constraints.PARTICIPANTE_APELIDO)) {
                throw new ErroNegocio(TipoErro.APELIDO_EM_USO, apelido);
            }
            throw erro;
        }
    }

    @Transactional(readOnly = true)
    public SalaRespostas.Estado estado(String codigoSala) {
        Sala sala = buscarSala(codigoSala);
        var participantes = participanteRepository.findBySalaOrderByCriadoEm(sala).stream()
                .map(ParticipanteResposta::de)
                .toList();
        var rodadaAtual = rodadaRepository.findFirstBySalaOrderByCriadoEmDesc(sala)
                .map(montadorDeRodada::montar)
                .orElse(null);
        return new SalaRespostas.Estado(
                sala.getCodigo(), sala.getNome(), BaralhoResposta.de(sala.getBaralho()), participantes, rodadaAtual);
    }

    public Sala buscarSala(String codigo) {
        return salaRepository.findByCodigo(normalizar(codigo))
                .orElseThrow(() -> new ErroNegocio(TipoErro.SALA_NAO_ENCONTRADA, codigo));
    }

    public Sala buscarSalaComTrava(String codigo) {
        return salaRepository.buscarComTrava(normalizar(codigo))
                .orElseThrow(() -> new ErroNegocio(TipoErro.SALA_NAO_ENCONTRADA, codigo));
    }

    /** Garante que o participante existe e pertence à sala. */
    public Participante buscarParticipante(Sala sala, UUID participanteId) {
        return participanteRepository.findByIdAndSala(participanteId, sala)
                .orElseThrow(() -> new ErroNegocio(TipoErro.PARTICIPANTE_FORA_DA_SALA));
    }

    /** Aceita "k7px3m" digitado em minúsculas. */
    private static String normalizar(String codigo) {
        return codigo.strip().toUpperCase(Locale.ROOT);
    }
}
