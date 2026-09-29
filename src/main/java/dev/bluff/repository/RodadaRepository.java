package dev.bluff.repository;

import dev.bluff.model.Rodada;
import dev.bluff.model.Sala;
import dev.bluff.model.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RodadaRepository extends JpaRepository<Rodada, UUID> {

    Optional<Rodada> findFirstBySalaOrderByCriadoEmDesc(Sala sala);

    List<Rodada> findBySalaOrderByCriadoEmDesc(Sala sala);

    List<Rodada> findBySalaAndStatusOrderByCriadoEmDesc(Sala sala, Status status);

    boolean existsBySalaAndStatus(Sala sala, Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Rodada r join fetch r.sala where r.id = :id")
    Optional<Rodada> buscarComTrava(UUID id);
}
