-- Script para verificar el esquema actual de la tabla usuario
-- Compatible con MySQL 5.7+ y MariaDB

USE PelisApp;

-- Mostrar la estructura de la tabla usuario
DESCRIBE usuario;

-- Verificar si existe el campo email_confirmed
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'PelisApp'
AND TABLE_NAME = 'usuario'
AND COLUMN_NAME = 'email_confirmed';

-- Verificar si existe el campo display_name
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'PelisApp'
AND TABLE_NAME = 'usuario'
AND COLUMN_NAME = 'display_name';

-- Contar usuarios existentes
SELECT COUNT(*) as total_usuarios FROM usuario;

-- Mostrar algunos usuarios de ejemplo (solo columnas básicas)
SELECT id, username, email
FROM usuario
LIMIT 5;
