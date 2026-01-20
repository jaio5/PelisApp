-- Añadir campo tmdb_profile_path a actor para conservar la path original en TMDb
ALTER TABLE actor ADD COLUMN IF NOT EXISTS tmdb_profile_path varchar(512);

