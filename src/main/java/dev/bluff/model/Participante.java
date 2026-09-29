package dev.bluff.model;

import dev.bluff.model.enums.Papel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "participante")
public class Participante {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "par_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sal_id", nullable = false)
    private Sala sala;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usu_id")
    private Usuario usuario;

    @Column(name = "par_apelido", nullable = false, length = 30)
    private String apelido;

    @Enumerated(EnumType.STRING)
    @Column(name = "par_papel", nullable = false, length = 20)
    private Papel papel;

    @Column(name = "par_criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected Participante() {
    }

    public Participante(Sala sala, String apelido, Papel papel) {
        this.sala = sala;
        this.apelido = apelido;
        this.papel = papel;
    }

    @PrePersist
    void aoCriar() {
        criadoEm = Instant.now();
    }

    public boolean podeVotar() {
        return papel == Papel.VOTANTE;
    }

    public UUID getId() { return id; }
    public Sala getSala() { return sala; }
    public Usuario getUsuario() { return usuario; }
    public String getApelido() { return apelido; }
    public Papel getPapel() { return papel; }
    public Instant getCriadoEm() { return criadoEm; }
}
