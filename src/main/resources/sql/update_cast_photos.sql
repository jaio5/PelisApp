-- Script SQL para MySQL - Añadir columnas de fotos de perfiles a Actor y Director
-- Ejecutar si las tablas ya existen y necesitas añadir las nuevas columnas

-- Verificar y añadir columna profile_local_path a la tabla actor
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'actor'
     AND COLUMN_NAME = 'profile_local_path') = 0,
    'ALTER TABLE actor ADD COLUMN profile_local_path VARCHAR(500) COMMENT ''Ruta local de la foto del actor descargada de TMDB''',
    'SELECT ''Column profile_local_path already exists in actor table'' AS message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verificar y añadir columna profile_local_path a la tabla director
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'director'
     AND COLUMN_NAME = 'profile_local_path') = 0,
    'ALTER TABLE director ADD COLUMN profile_local_path VARCHAR(500) COMMENT ''Ruta local de la foto del director descargada de TMDB''',
    'SELECT ''Column profile_local_path already exists in director table'' AS message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Crear índices para mejorar performance en búsquedas por TMDB ID (solo si no existen)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'actor'
     AND INDEX_NAME = 'idx_actor_tmdb_id') = 0,
    'CREATE INDEX idx_actor_tmdb_id ON actor(tmdb_id)',
    'SELECT ''Index idx_actor_tmdb_id already exists'' AS message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'director'
     AND INDEX_NAME = 'idx_director_tmdb_id') = 0,
    'CREATE INDEX idx_director_tmdb_id ON director(tmdb_id)',
    'SELECT ''Index idx_director_tmdb_id already exists'' AS message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verificar estructura actualizada
SELECT 'actor table structure:' AS info;
DESCRIBE actor;

SELECT 'director table structure:' AS info;
DESCRIBE director;

-- Mostrar índices creados
SELECT 'actor indexes:' AS info;
SHOW INDEX FROM actor WHERE Key_name LIKE '%tmdb%';

SELECT 'director indexes:' AS info;
SHOW INDEX FROM director WHERE Key_name LIKE '%tmdb%';

