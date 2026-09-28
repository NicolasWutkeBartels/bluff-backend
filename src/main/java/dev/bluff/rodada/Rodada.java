package dev.bluff.rodada;

import dev.bluff.sala.Sala;
import dev.bluff.status.Status;
import dev.bluff.status.StatusRodada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rodada")
public class Rodada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rod_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sal_id", nullable = false)
    private Sala sala;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sta_id", nullable = false)
    private Status status;

    @Column(name = "rod_titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "rod_estimativa_final", length = 10)
    private String estimativaFinal;

    @Column(name = "rod_criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected Rodada() {
        // exigido pelo JPA
    }

    public Rodada(Sala sala, Status emVotacao, String titulo) {
        this.sala = sala;
        this.status = emVotacao;
        this.titulo = titulo;
    }

    @PrePersist
    void aoCriar() {
        criadoEm = Instant.now();
    }

    public boolean estaEmVotacao() {
        return status.temCodigo(StatusRodada.VOTING);
    }

    public boolean foiRevelada() {
        return status.temCodigo(StatusRodada.REVEALED);
    }

    public void revelar(Status revelada, String estimativaFinal) {
        this.status = revelada;
        this.estimativaFinal = estimativaFinal;
    }

    public UUID getId() { return id; }
    public Sala getSala() { return sala; }
    public Status getStatus() { return status; }
    public String getTitulo() { return titulo; }
    public String getEstimativaFinal() { return estimativaFinal; }
    public Instant getCriadoEm() { return criadoEm; }
}
