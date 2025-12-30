ALTER TABLE usuarios
    ADD COLUMN refresh_token VARCHRA(512),
    ADD COLUMN expiracao_refresh_token TIMESTAMP;