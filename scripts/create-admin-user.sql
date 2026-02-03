-- Script para crear cuenta de administrador en PelisApp
-- Crear roles necesarios y usuario admin

USE pelisapp;

-- 1. Crear roles necesarios si no existen
INSERT IGNORE INTO role (name, description) VALUES
('ADMIN', 'Administrador del sistema'),
('MODERATOR', 'Moderador de contenido'),
('SUPERADMIN', 'Super administrador');

-- 2. Crear usuario administrador
-- Contraseña: admin123 (encriptada con BCrypt)
INSERT INTO usuario (username, email, password, display_name, registered_at, email_confirmed, critic_level)
VALUES (
    'admin',
    'admin@pelisapp.com',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd4zMBf9AnZC72G2', -- admin123
    'Administrador',
    NOW(),
    1,
    0
);

-- 3. Obtener el ID del usuario admin recién creado
SET @admin_user_id = LAST_INSERT_ID();

-- 4. Asignar todos los roles al admin
INSERT INTO usuario_roles (usuario_id, role_id)
SELECT @admin_user_id, id FROM role WHERE name IN ('ADMIN', 'MODERATOR', 'SUPERADMIN');

-- 5. Mostrar resultado
SELECT 'USUARIO ADMIN CREADO:' as info;
SELECT
    u.id,
    u.username,
    u.email,
    u.display_name,
    u.email_confirmed,
    GROUP_CONCAT(r.name SEPARATOR ', ') as roles
FROM usuario u
LEFT JOIN usuario_roles ur ON u.id = ur.usuario_id
LEFT JOIN role r ON ur.role_id = r.id
WHERE u.username = 'admin'
GROUP BY u.id;
