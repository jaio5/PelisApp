-- Añadir columna tmdb_id a actor y director y crear índices
ALTER TABLE actor ADD COLUMN IF NOT EXISTS tmdb_id integer;
ALTER TABLE director ADD COLUMN IF NOT EXISTS tmdb_id integer;

-- Índices únicos para evitar duplicados por tmdb_id
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE tablename='actor' AND indexname='idx_actor_tmdb_id') THEN
        CREATE UNIQUE INDEX idx_actor_tmdb_id ON actor (tmdb_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE tablename='director' AND indexname='idx_director_tmdb_id') THEN
        CREATE UNIQUE INDEX idx_director_tmdb_id ON director (tmdb_id);
    END IF;
END$$;
