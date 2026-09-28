package dev.bluff.status;

import dev.bluff.erro.ErroNegocio;
import dev.bluff.erro.TipoErro;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StatusService {

    private final StatusRepository statusRepository;

    public StatusService(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    /** Valores de um domínio, para preencher selects. Domínio inexistente devolve lista vazia. */
    public List<Status> listarPorDominio(String dominio) {
        return statusRepository.findByDominioOrderByDescricao(dominio);
    }

    /**
     * Único ponto do sistema que transforma um código de status na entidade.
     * Nunca buscamos status pelo id: o UUID muda entre ambientes, o código não.
     */
    public Status buscarStatus(String dominio, String codigo) {
        return statusRepository.findByDominioAndCodigo(dominio, codigo)
                .orElseThrow(() -> new ErroNegocio(TipoErro.STATUS_INEXISTENTE, codigo, dominio));
    }
}
