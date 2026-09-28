package dev.bluff.status;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StatusRepository extends JpaRepository<Status, UUID> {

    Optional<Status> findByDominioAndCodigo(String dominio, String codigo);

    List<Status> findByDominioOrderByDescricao(String dominio);
}
