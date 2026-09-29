package dev.bluff.model;

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
@Table(name = "voto")
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "vot_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rod_id", nullable = false)
    private Rodada rodada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "par_id", nullable = false)
    private Participante participante;

    @Column(name = "vot_valor", nullable = false, length = 10)
    private String valor;

    @Column(name = "vot_criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected Voto() {
    }

    public Voto(Rodada rodada, Participante participante, String valor) {
        this.rodada = rodada;
        this.participante = participante;
        this.valor = valor;
    }

    @PrePersist
    void aoCriar() {
        criadoEm = Instant.now();
    }

    public void trocarValor(String novoValor) {
        this.valor = novoValor;
    }

    public UUID getId() { return id; }
    public Rodada getRodada() { return rodada; }
    public Participante getParticipante() { return participante; }
    public String getValor() { return valor; }
    public Instant getCriadoEm() { return criadoEm; }
}
