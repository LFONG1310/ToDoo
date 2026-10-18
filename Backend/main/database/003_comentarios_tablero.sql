-- Conversacion de cada tablero entre sus responsables.
-- Ejecutar despues de 002_usuarios_tableros.sql.

CREATE TABLE comentarios_tablero (
    id INT NOT NULL AUTO_INCREMENT,
    tablero_id INT NOT NULL,
    usuario_id INT NOT NULL,
    texto VARCHAR(1000) NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    -- NULL mientras el comentario no se haya editado.
    fecha_edicion DATETIME(6) NULL,
    PRIMARY KEY (id),
    INDEX idx_comentarios_tablero_fecha (tablero_id, fecha_creacion, id),
    CONSTRAINT fk_comentarios_tablero FOREIGN KEY (tablero_id) REFERENCES tableros (id) ON DELETE CASCADE,
    -- Sin cascada: para "borrar" a alguien se desactiva (usuarios.activo) y su historial se conserva.
    CONSTRAINT fk_comentarios_autor FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
