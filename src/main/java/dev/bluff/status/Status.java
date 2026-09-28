package dev.bluff.status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Valor fixo do sistema. Os registros vêm da migration V2 e são somente leitura.
 * Quem precisa de um status busca por domínio + código em {@link StatusService#buscarStatus}.
 */
@Entity
@Table(name = "status")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sta_id")
    private UUID id;

    @Column(name = "sta_dominio", nullable = false, length = 50)
    private String dominio;

    @Column(name = "sta_codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "sta_descricao", nullable = false, length = 100)
    private String descricao;

    protected Status() {
        // exigido pelo JPA
    }

    public boolean temCodigo(String codigo) {
        return this.codigo.equals(codigo);
    }

    public UUID getId() { return id; }
    public String getDominio() { return dominio; }
    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
}
