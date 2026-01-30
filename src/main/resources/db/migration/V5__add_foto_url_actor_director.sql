-- Añadir columna foto_url a actor y director para almacenar URL de la imagen importada desde TMDb
ALTER TABLE actor ADD COLUMN IF NOT EXISTS foto_url varchar(1024);
ALTER TABLE director ADD COLUMN IF NOT EXISTS foto_url varchar(1024);
