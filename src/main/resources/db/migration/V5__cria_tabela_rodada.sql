CREATE TABLE rodada (
    rod_id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    sal_id               UUID         NOT NULL,
    sta_id               UUID         NOT NULL,
    rod_titulo           VARCHAR(200) NOT NULL,
    rod_estimativa_final VARCHAR(10),
    rod_criado_em        TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_rodada PRIMARY KEY (rod_id),
    CONSTRAINT fk_rodada_sala FOREIGN KEY (sal_id) REFERENCES sala (sal_id),
    CONSTRAINT fk_rodada_status FOREIGN KEY (sta_id) REFERENCES status (sta_id)
);

-- O histórico lista as rodadas da sala da mais nova para a mais antiga.
CREATE INDEX ix_rodada_sal_id_criado_em ON rodada (sal_id, rod_criado_em DESC);
CREATE INDEX ix_rodada_sta_id ON rodada (sta_id);
