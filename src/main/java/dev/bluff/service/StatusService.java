package dev.bluff.service;

import dev.bluff.exception.NegocioException;
import dev.bluff.exception.TipoErro;
import dev.bluff.model.Status;
import dev.bluff.repository.StatusRepository;
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

    public List<Status> listarPorDominio(String dominio) {
        return statusRepository.findByDominioOrderByDescricao(dominio);
    }

    public Status buscarPorCodigo(String dominio, String codigo) {
        return statusRepository.findByDominioAndCodigo(dominio, codigo)
                .orElseThrow(() -> new NegocioException(TipoErro.STATUS_INEXISTENTE, codigo, dominio));
    }
}
