CREATE TABLE voto (
    vot_id        UUID        NOT NULL DEFAULT gen_random_uuid(),
    rod_id        UUID        NOT NULL,
    par_id        UUID        NOT NULL,
    vot_valor     VARCHAR(10) NOT NULL,
    vot_criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT pk_voto PRIMARY KEY (vot_id),
    CONSTRAINT fk_voto_rodada FOREIGN KEY (rod_id) REFERENCES rodada (rod_id),
    CONSTRAINT fk_voto_participante FOREIGN KEY (par_id) REFERENCES participante (par_id),
    -- Um voto por participante por rodada. Votar de novo atualiza esta linha.
    CONSTRAINT uk_voto_rodada_participante UNIQUE (rod_id, par_id)
);

CREATE INDEX ix_voto_par_id ON voto (par_id);
