CREATE TABLE participante (
    par_id        UUID        NOT NULL DEFAULT gen_random_uuid(),
    sal_id        UUID        NOT NULL,
    usu_id        UUID,                                     -- nulo enquanto não houver login
    par_apelido   VARCHAR(30) NOT NULL,
    par_papel     VARCHAR(20) NOT NULL DEFAULT 'VOTANTE',   -- validado pelo enum Papel no Java
    par_criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_participante PRIMARY KEY (par_id),
    CONSTRAINT fk_participante_sala FOREIGN KEY (sal_id) REFERENCES sala (sal_id),
    CONSTRAINT fk_participante_usuario FOREIGN KEY (usu_id) REFERENCES usuario (usu_id),
    CONSTRAINT uk_participante_sala_apelido UNIQUE (sal_id, par_apelido)
);

CREATE INDEX ix_participante_usu_id ON participante (usu_id);
