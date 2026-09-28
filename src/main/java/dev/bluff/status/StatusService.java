package dev.bluff.status;

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
}
