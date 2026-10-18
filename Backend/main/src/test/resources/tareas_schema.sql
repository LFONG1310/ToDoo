-- Subconjunto de database/propuesta_mysql.sql para las pruebas en H2 (modo MySQL).
-- Conservar los mismos nombres de columnas que usa TareaRepository.
CREATE TABLE usuarios (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE miembros_proyecto (
    proyecto_id INT NOT NULL,
    usuario_id INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (proyecto_id, usuario_id)
);

CREATE TABLE tareas (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    proyecto_id INT NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    descripcion TEXT NOT NULL,
    responsable_id INT NOT NULL,
    prioridad VARCHAR(10) NOT NULL DEFAULT 'media',
    estado VARCHAR(20) NOT NULL DEFAULT 'todo',
    posicion INT NOT NULL DEFAULT 0,
    fecha_limite DATETIME(6) NOT NULL,
    archivada BOOLEAN NOT NULL DEFAULT FALSE
);
