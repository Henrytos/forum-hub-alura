ALTER TABLE respostas
    DROP COLUMN autor,
    ADD COLUMN autor_id BIGINT,
    ADD CONSTRAINT fk_respostas_usuario FOREIGN KEY (autor_id) REFERENCES usuarios (id)
;