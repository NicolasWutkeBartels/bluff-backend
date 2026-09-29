package dev.bluff.repository;

import dev.bluff.model.Participante;
import dev.bluff.model.Rodada;
import dev.bluff.model.Voto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VotoRepository extends JpaRepository<Voto, UUID> {

    Optional<Voto> findByRodadaAndParticipante(Rodada rodada, Participante participante);

    @Query("select v from Voto v join fetch v.participante where v.rodada in :rodadas order by v.criadoEm")
    List<Voto> buscarPorRodadas(Collection<Rodada> rodadas);
}
