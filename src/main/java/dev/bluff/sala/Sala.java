package dev.bluff.sala;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sala")
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sal_id")
    private UUID id;

    @Column(name = "sal_codigo", nullable = false, unique = true, length = 6)
    private String codigo;

    @Column(name = "sal_nome", nullable = false, length = 80)
    private String nome;

    // Texto no banco, enum no Java: um valor inválido nem chega ao banco.
    @Enumerated(EnumType.STRING)
    @Column(name = "sal_baralho", nullable = false, length = 20)
    private Baralho baralho;

    @Column(name = "sal_criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected Sala() {
        // exigido pelo JPA
    }

    public Sala(String codigo, String nome, Baralho baralho) {
        this.codigo = codigo;
        this.nome = nome;
        this.baralho = baralho;
    }

    @PrePersist
    void aoCriar() {
        criadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public Baralho getBaralho() { return baralho; }
    public Instant getCriadoEm() { return criadoEm; }
}
