CREATE TABLE perfies(
                       id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                       nome VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE usuarios_perfies(
                                usuario_id BIGINT NOT NULL,
                                perfil_id BIGINT NOT NULL,

                                PRIMARY KEY(usuario_id, perfil_id),
                                CONSTRAINT USUARIOS_PERFIS_FK_USUARIO FOREIGN KEY(usuario_id) REFERENCES usuarios(id),
                                CONSTRAINT USUARIOS_PERFIS_FK_PERFIL FOREIGN KEY(perfil_id) REFERENCES perfies(id)

);

INSERT INTO perfies(nome) VALUES('ESTUDANTE');
INSERT INTO perfies(nome) VALUES('INSTRUTOR');
INSERT INTO perfies(nome) VALUES('MODERADOR');
INSERT INTO perfies(nome) VALUES('ADMIN');