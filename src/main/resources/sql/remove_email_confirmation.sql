-- Script para eliminar completamente el sistema de confirmación por email
-- Ejecutar después de actualizar el código

-- Eliminar la columna email_confirmed de la tabla usuario
ALTER TABLE usuario DROP COLUMN email_confirmed;

-- Opcional: Verificar que la columna fue eliminada
DESCRIBE usuario;

-- Nota: Este script debe ejecutarse después de desplegar el código actualizado
-- para evitar errores al inicio de la aplicación.
