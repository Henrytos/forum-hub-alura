CREATE TABLE usuarios_perfies
(`perfil_id ` BIGINT NOT NULL,
    usuario_id   BIGINT NOT NULL
);

ALTER TABLE usuarios_perfies
    ADD CONSTRAINT fk_usuper_on_perfil FOREIGN KEY (perfil_id) REFERENCES perfies (id);

ALTER TABLE usuarios_perfies
    ADD CONSTRAINT fk_usuper_on_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id);

ALTER TABLE usuarios
    MODIFY biografia VARCHAR (255);

ALTER TABLE usuarios
    MODIFY email VARCHAR (255);

ALTER TABLE usuarios
    MODIFY email VARCHAR (255) NULL;

ALTER TABLE usuarios
    MODIFY mini_biografia VARCHAR (255);

ALTER TABLE usuarios
    MODIFY nome_completo VARCHAR (255);

ALTER TABLE usuarios
    MODIFY nome_completo VARCHAR (255) NULL;

ALTER TABLE usuarios
    MODIFY nome_usuario VARCHAR (255);

ALTER TABLE usuarios
    MODIFY nome_usuario VARCHAR (255) NULL;

ALTER TABLE usuarios
    MODIFY senha VARCHAR (255);

ALTER TABLE usuarios
    MODIFY senha VARCHAR (255) NULL;

ALTER TABLE usuarios
    MODIFY token VARCHAR (255);

ALTER TABLE usuarios
    MODIFY verificado BIT (1) NULL;