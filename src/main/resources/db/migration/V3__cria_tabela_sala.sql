CREATE TABLE sala (
    sal_id        UUID         NOT NULL DEFAULT gen_random_uuid(),
    sal_codigo    VARCHAR(6)   NOT NULL,
    sal_nome      VARCHAR(80)  NOT NULL,
    sal_baralho   VARCHAR(20)  NOT NULL DEFAULT 'FIBONACCI', -- validado pelo enum Baralho no Java
    sal_criado_em TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_sala PRIMARY KEY (sal_id),
    CONSTRAINT uk_sala_codigo UNIQUE (sal_codigo)
);
