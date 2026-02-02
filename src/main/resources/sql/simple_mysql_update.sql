-- Script SQL SIMPLE para MySQL - Añadir columnas de reparto
-- Ejecutar MANUALMENTE en tu cliente MySQL

-- Cambiar a la base de datos correcta
USE pelisapp;

-- Añadir columnas si no existen (sintaxis MySQL pura)
ALTER TABLE actor ADD COLUMN profile_local_path VARCHAR(500) NULL COMMENT 'Ruta local de la foto del actor';
ALTER TABLE director ADD COLUMN profile_local_path VARCHAR(500) NULL COMMENT 'Ruta local de la foto del director';

-- Crear índices
CREATE INDEX idx_actor_tmdb_id ON actor(tmdb_id);
CREATE INDEX idx_director_tmdb_id ON director(tmdb_id);

-- Verificar que se añadieron correctamente
DESCRIBE actor;
DESCRIBE director;

-- Mostrar algunas filas para verificar
SELECT COUNT(*) as total_actors FROM actor;
SELECT COUNT(*) as total_directors FROM director;

-- Verificar índices
SHOW INDEX FROM actor WHERE Key_name LIKE '%tmdb%';
SHOW INDEX FROM director WHERE Key_name LIKE '%tmdb%';
