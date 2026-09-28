-- Valores fixos do sistema, agrupados por domínio (ex.: RODADA).
-- O código é o identificador estável entre ambientes; o UUID muda a cada banco.
CREATE TABLE status (
    sta_id        UUID         NOT NULL DEFAULT gen_random_uuid(),
    sta_dominio   VARCHAR(50)  NOT NULL,
    sta_codigo    VARCHAR(50)  NOT NULL,
    sta_descricao VARCHAR(100) NOT NULL,

    CONSTRAINT pk_status PRIMARY KEY (sta_id),
    CONSTRAINT uk_status_dominio_codigo UNIQUE (sta_dominio, sta_codigo)
);

INSERT INTO status (sta_dominio, sta_codigo, sta_descricao) VALUES
    ('RODADA', 'VOTING',   'Em votação'),
    ('RODADA', 'REVEALED', 'Revelada');
