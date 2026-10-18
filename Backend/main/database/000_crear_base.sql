-- Ejecutar UNA sola vez con un usuario administrador de MySQL (por ejemplo root) en HeidiSQL.
-- Crea la base de datos y el usuario que usa el backend.
-- Cambia 'CambiaEstaContrasena' antes de ejecutar y usa la misma en DB_PASSWORD
-- (o en Backend/main/application-local.properties).

CREATE DATABASE IF NOT EXISTS todoo
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'todoo'@'localhost' IDENTIFIED BY 'CambiaEstaContrasena';
GRANT SELECT, INSERT, UPDATE, DELETE ON todoo.* TO 'todoo'@'localhost';
FLUSH PRIVILEGES;
