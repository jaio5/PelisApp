-- V4__create_etiqueta_table.sql

CREATE TABLE IF NOT EXISTS etiqueta (
    id serial PRIMARY KEY,
    clave varchar(100) NOT NULL UNIQUE,
    nombre varchar(200) NOT NULL,
    descripcion text,
    icono_url varchar(512)
);

-- join table usuario_etiquetas (si no existe ya)
CREATE TABLE IF NOT EXISTS usuario_etiquetas (
    usuario_id integer NOT NULL,
    etiqueta_id integer NOT NULL,
    PRIMARY KEY (usuario_id, etiqueta_id)
);
