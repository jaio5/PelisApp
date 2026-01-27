-- Añadir campo tmdb_profile_path a director para conservar la path original en TMDb
ALTER TABLE director ADD COLUMN IF NOT EXISTS tmdb_profile_path varchar(512);

