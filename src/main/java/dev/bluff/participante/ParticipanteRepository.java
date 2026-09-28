package dev.bluff.participante;

import dev.bluff.sala.Sala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticipanteRepository extends JpaRepository<Participante, UUID> {

    List<Participante> findBySalaOrderByCriadoEm(Sala sala);

    Optional<Participante> findByIdAndSala(UUID id, Sala sala);
}
