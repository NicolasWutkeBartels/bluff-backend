package dev.bluff.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Usuário com login. Ainda não é usado: existe para o login futuro
 * ligar um participante a uma pessoa (participante.usu_id).
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "usu_id")
    private UUID id;

    @Column(name = "usu_nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "usu_email", nullable = false, unique = true)
    private String email;

    @Column(name = "usu_senha", nullable = false, length = 100)
    private String senha;

    @Column(name = "usu_criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected Usuario() {
        // exigido pelo JPA
    }

    public Usuario(String nome, String email, String senhaHash) {
        this.nome = nome;
        this.email = email;
        this.senha = senhaHash;
    }

    @PrePersist
    void aoCriar() {
        criadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public Instant getCriadoEm() { return criadoEm; }
}
