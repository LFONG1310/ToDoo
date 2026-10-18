CREATE TABLE comentarios (
    id INT NOT NULL AUTO_INCREMENT,
    tarea_id INT NOT NULL,
    usuario_id INT NOT NULL,
    texto VARCHAR(1000) NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_comentarios_tarea (tarea_id, fecha_creacion, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;