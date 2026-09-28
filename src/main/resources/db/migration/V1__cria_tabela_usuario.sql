-- Usuário com login. Ainda não é usado pela aplicação: existe para que o login
-- futuro não precise mexer em tabelas que já estão em produção.
CREATE TABLE usuario (
    usu_id        UUID         NOT NULL DEFAULT gen_random_uuid(),
    usu_nome      VARCHAR(100) NOT NULL,
    usu_email     VARCHAR(255) NOT NULL,
    usu_senha     VARCHAR(100) NOT NULL, -- hash BCrypt, nunca a senha em texto
    usu_criado_em TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT pk_usuario PRIMARY KEY (usu_id),
    CONSTRAINT uk_usuario_email UNIQUE (usu_email)
);
