-- Script para pgAdmin - esquema PelisApp (crear tablas, constraints y triggers)
BEGIN;

-- Tabla usuario
CREATE TABLE IF NOT EXISTS usuario (
  id                integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  username          varchar(50) NOT NULL UNIQUE,
  password          varchar(255) NOT NULL,
  nivel_critico     integer NOT NULL DEFAULT 1,
  fecha_registro    timestamptz NOT NULL DEFAULT now()
);

-- Tabla pais
CREATE TABLE IF NOT EXISTS pais (
  id   integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre varchar(100) NOT NULL
);

-- Tabla pelicula
CREATE TABLE IF NOT EXISTS pelicula (
  id            integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  titulo        varchar(200) NOT NULL,
  anio          integer NOT NULL,
  duracion      integer,
  sinopsis      text,
  pais_id       integer REFERENCES pais(id) ON DELETE SET NULL,
  fecha_estreno date
);

-- Tabla categoria
CREATE TABLE IF NOT EXISTS categoria (
  id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre varchar(100) NOT NULL,
  descripcion text
);

-- Tabla director
CREATE TABLE IF NOT EXISTS director (
  id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre varchar(150) NOT NULL,
  fecha_nacimiento date,
  pais_id integer REFERENCES pais(id) ON DELETE SET NULL,
  biografia text
);

-- Tabla actor
CREATE TABLE IF NOT EXISTS actor (
  id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre varchar(150) NOT NULL,
  fecha_nacimiento date,
  pais_id integer REFERENCES pais(id) ON DELETE SET NULL,
  biografia text
);

-- Tablas intermedias ManyToMany
CREATE TABLE IF NOT EXISTS pelicula_categoria (
  pelicula_id integer NOT NULL REFERENCES pelicula(id) ON DELETE CASCADE,
  categoria_id integer NOT NULL REFERENCES categoria(id) ON DELETE CASCADE,
  PRIMARY KEY (pelicula_id, categoria_id)
);

CREATE TABLE IF NOT EXISTS pelicula_director (
  pelicula_id integer NOT NULL REFERENCES pelicula(id) ON DELETE CASCADE,
  director_id integer NOT NULL REFERENCES director(id) ON DELETE CASCADE,
  PRIMARY KEY (pelicula_id, director_id)
);

CREATE TABLE IF NOT EXISTS pelicula_actor (
  pelicula_id integer NOT NULL REFERENCES pelicula(id) ON DELETE CASCADE,
  actor_id integer NOT NULL REFERENCES actor(id) ON DELETE CASCADE,
  PRIMARY KEY (pelicula_id, actor_id)
);

-- Tabla reseña (puntuación en estrellas 1..5)
CREATE TABLE IF NOT EXISTS resena (
  id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  usuario_id integer NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
  pelicula_id integer NOT NULL REFERENCES pelicula(id) ON DELETE CASCADE,
  puntuacion integer CHECK (puntuacion >= 1 AND puntuacion <= 5) NOT NULL,
  comentario text,
  fecha timestamptz NOT NULL DEFAULT now()
);

-- Tabla valoracion_resena
CREATE TABLE IF NOT EXISTS valoracion_resena (
  id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  resena_id integer NOT NULL REFERENCES resena(id) ON DELETE CASCADE,
  valorador_id integer NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
  puntuacion integer NOT NULL CHECK (puntuacion >= 1 AND puntuacion <= 5),
  comentario text,
  fecha timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT valoracion_resena_unica UNIQUE (resena_id, valorador_id)
);

-- Tabla seguidores (usuario sigue a usuario) -> usuario_seguidores
CREATE TABLE IF NOT EXISTS usuario_seguidores (
  usuario_id integer NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
  seguido_id integer NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
  PRIMARY KEY (usuario_id, seguido_id)
);

-- Tablas para roles y etiquetas (colecciones)
CREATE TABLE IF NOT EXISTS usuario_roles (
  usuario_id integer NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
  role varchar(50) NOT NULL,
  PRIMARY KEY (usuario_id, role)
);

CREATE TABLE IF NOT EXISTS usuario_etiquetas (
  usuario_id integer NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
  etiqueta varchar(100) NOT NULL,
  PRIMARY KEY (usuario_id, etiqueta)
);

-- FUNCION: impedir que un usuario valore su propia reseña
CREATE OR REPLACE FUNCTION prevent_self_valoracion() RETURNS trigger AS $$
DECLARE
  autor_id integer;
BEGIN
  -- Cuando se inserta/actualiza, comprobamos que el valorador no sea el autor de la reseña
  SELECT usuario_id INTO autor_id FROM resena WHERE id = NEW.resena_id;
  IF autor_id IS NULL THEN
    RAISE EXCEPTION 'Reseña % no encontrada', NEW.resena_id;
  END IF;
  IF autor_id = NEW.valorador_id THEN
    RAISE EXCEPTION 'No se permite valorar la propia reseña (usuario = %)', NEW.valorador_id;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger que aplica la función antes de INSERT o UPDATE
DROP TRIGGER IF EXISTS trg_prevent_self_valoracion ON valoracion_resena;
CREATE TRIGGER trg_prevent_self_valoracion
  BEFORE INSERT OR UPDATE ON valoracion_resena
  FOR EACH ROW
  EXECUTE FUNCTION prevent_self_valoracion();

-- FUNCION: recalcula nivel_critico del autor de las reseñas afectadas
CREATE OR REPLACE FUNCTION update_nivel_critico_after_valoracion() RETURNS trigger AS $$
DECLARE
  affected_usuario integer;
  avg_puntuacion numeric;
  nuevo_nivel integer;
  old_autor integer;
  new_autor integer;
BEGIN
  IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
    -- Obtener el autor de la reseña nueva
    SELECT usuario_id INTO affected_usuario FROM resena WHERE id = NEW.resena_id;
    -- Si UPDATE y cambió resena_id, también actualizar al autor anterior
    IF TG_OP = 'UPDATE' THEN
      SELECT usuario_id INTO old_autor FROM resena WHERE id = OLD.resena_id;
      IF old_autor IS NOT NULL AND old_autor <> affected_usuario THEN
        -- recalcular para old_autor
        SELECT avg(v.puntuacion) INTO avg_puntuacion FROM valoracion_resena v JOIN resena r ON v.resena_id = r.id WHERE r.usuario_id = old_autor;
        IF avg_puntuacion IS NULL THEN
          nuevo_nivel := 1;
        ELSE
          nuevo_nivel := GREATEST(1, LEAST(5, CAST(round(avg_puntuacion) AS integer)));
        END IF;
        UPDATE usuario SET nivel_critico = nuevo_nivel WHERE id = old_autor;
      END IF;
    END IF;

    -- recalcular para affected_usuario (new)
    SELECT avg(v.puntuacion) INTO avg_puntuacion FROM valoracion_resena v JOIN resena r ON v.resena_id = r.id WHERE r.usuario_id = affected_usuario;
    IF avg_puntuacion IS NULL THEN
      nuevo_nivel := 1;
    ELSE
      nuevo_nivel := GREATEST(1, LEAST(5, CAST(round(avg_puntuacion) AS integer)));
    END IF;
    UPDATE usuario SET nivel_critico = nuevo_nivel WHERE id = affected_usuario;
    RETURN NEW;

  ELSIF TG_OP = 'DELETE' THEN
    -- En DELETE, usamos OLD.resena_id para buscar autor
    SELECT usuario_id INTO affected_usuario FROM resena WHERE id = OLD.resena_id;
    IF affected_usuario IS NOT NULL THEN
      SELECT avg(v.puntuacion) INTO avg_puntuacion FROM valoracion_resena v JOIN resena r ON v.resena_id = r.id WHERE r.usuario_id = affected_usuario;
      IF avg_puntuacion IS NULL THEN
        nuevo_nivel := 1;
      ELSE
        nuevo_nivel := GREATEST(1, LEAST(5, CAST(round(avg_puntuacion) AS integer)));
      END IF;
      UPDATE usuario SET nivel_critico = nuevo_nivel WHERE id = affected_usuario;
    END IF;
    RETURN OLD;
  END IF;

  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger AFTER INSERT/UPDATE/DELETE para mantener nivel_critico consistente
DROP TRIGGER IF EXISTS trg_valoracion_after_change ON valoracion_resena;
CREATE TRIGGER trg_valoracion_after_change
  AFTER INSERT OR UPDATE OR DELETE ON valoracion_resena
  FOR EACH ROW
  EXECUTE FUNCTION update_nivel_critico_after_valoracion();

COMMIT;

-- Inserts de ejemplo (opcionales)
-- INSERT INTO usuario (username, password) VALUES ('u1', 'password-hash'), ('u2', 'password-hash2');
-- INSERT INTO pais (nombre) VALUES ('España');
-- INSERT INTO pelicula (titulo, anio, pais_id) VALUES ('Mi Pelicula', 2020, 1);

