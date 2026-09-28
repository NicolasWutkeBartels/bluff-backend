package dev.bluff.voto;

import dev.bluff.participante.Participante;
import dev.bluff.rodada.Rodada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VotoRepository extends JpaRepository<Voto, UUID> {

    Optional<Voto> findByRodadaAndParticipante(Rodada rodada, Participante participante);

    /** Votos de várias rodadas numa consulta só (evita N+1 no histórico). */
    @Query("select v from Voto v join fetch v.participante where v.rodada in :rodadas order by v.criadoEm")
    List<Voto> buscarPorRodadas(Collection<Rodada> rodadas);
}
