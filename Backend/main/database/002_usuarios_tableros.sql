-- Usuarios, tableros y responsables (miembros) de cada tablero.
-- Ejecutar sobre la base todoo despues de 001_notificaciones.sql.

CREATE TABLE usuarios (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(255) NOT NULL,
    -- Formato: pbkdf2_sha256$iteraciones$sal$hash (ver ContrasenaHasher.java). Nunca texto plano.
    contrasena_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_correo UNIQUE (correo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tableros (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500) NULL,
    creado_por INT NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_tableros_creador FOREIGN KEY (creado_por) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Quien participa en cada tablero. Solo los miembros pueden ver y escribir comentarios.
-- PROPIETARIO: administra el tablero y puede moderar (eliminar) comentarios.
-- RESPONSABLE: participa en el tablero y en la conversacion.
CREATE TABLE tablero_miembros (
    tablero_id INT NOT NULL,
    usuario_id INT NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'RESPONSABLE',
    fecha_union DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (tablero_id, usuario_id),
    INDEX idx_miembros_usuario (usuario_id),
    CONSTRAINT ck_miembros_rol CHECK (rol IN ('PROPIETARIO', 'RESPONSABLE')),
    CONSTRAINT fk_miembros_tablero FOREIGN KEY (tablero_id) REFERENCES tableros (id) ON DELETE CASCADE,
    CONSTRAINT fk_miembros_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
