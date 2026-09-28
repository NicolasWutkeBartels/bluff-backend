package dev.bluff.sala;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SalaRepository extends JpaRepository<Sala, UUID> {

    Optional<Sala> findByCodigo(String codigo);

    /**
     * Busca a sala com SELECT ... FOR UPDATE. Duas pessoas clicando em "Começar rodada"
     * ao mesmo tempo passam por aqui uma de cada vez, o que garante uma só rodada em votação.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Sala s where s.codigo = :codigo")
    Optional<Sala> buscarComTrava(String codigo);
}
