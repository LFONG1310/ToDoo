-- COPIA para pruebas (H2 en modo MySQL) de database/propuesta_mysql.sql, sin CREATE DATABASE/USE/SET.
-- No editar a mano: EsquemaPruebasTest falla si deja de coincidir con la propuesta.

CREATE TABLE usuarios (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  correo VARCHAR(254) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  UNIQUE KEY uq_usuarios_correo (correo)
) ENGINE=InnoDB;

CREATE TABLE proyectos (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL,
  descripcion TEXT NOT NULL,
  creado_por INT NOT NULL,
  archivado BOOLEAN NOT NULL DEFAULT FALSE,
  fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT fk_proyectos_creador FOREIGN KEY (creado_por)
    REFERENCES usuarios(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE miembros_proyecto (
  proyecto_id INT NOT NULL,
  usuario_id INT NOT NULL,
  rol ENUM('administrador', 'miembro') NOT NULL DEFAULT 'miembro',
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_ingreso DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (proyecto_id, usuario_id),
  CONSTRAINT fk_miembros_proyecto FOREIGN KEY (proyecto_id)
    REFERENCES proyectos(id) ON DELETE RESTRICT,
  CONSTRAINT fk_miembros_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE tareas (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  proyecto_id INT NOT NULL,
  titulo VARCHAR(100) NOT NULL,
  descripcion TEXT NOT NULL,
  responsable_id INT NOT NULL,
  creado_por INT NOT NULL,
  prioridad ENUM('alta', 'media', 'baja') NOT NULL DEFAULT 'media',
  estado ENUM('todo', 'in-progress', 'done') NOT NULL DEFAULT 'todo',
  posicion INT NOT NULL DEFAULT 0,
  fecha_limite DATETIME(6) NOT NULL,
  archivada BOOLEAN NOT NULL DEFAULT FALSE,
  fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  fecha_actualizacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uq_tareas_proyecto_id (proyecto_id, id),
  KEY idx_tareas_tablero (proyecto_id, estado, posicion, id),
  KEY idx_tareas_responsable_fecha (responsable_id, estado, fecha_limite),
  CONSTRAINT chk_tareas_titulo CHECK (CHAR_LENGTH(TRIM(titulo)) > 0),
  CONSTRAINT chk_tareas_posicion CHECK (posicion >= 0),
  CONSTRAINT fk_tareas_proyecto FOREIGN KEY (proyecto_id)
    REFERENCES proyectos(id) ON DELETE RESTRICT,
  CONSTRAINT fk_tareas_responsable FOREIGN KEY (proyecto_id, responsable_id)
    REFERENCES miembros_proyecto(proyecto_id, usuario_id) ON DELETE RESTRICT,
  CONSTRAINT fk_tareas_creador FOREIGN KEY (proyecto_id, creado_por)
    REFERENCES miembros_proyecto(proyecto_id, usuario_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE comentarios (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  proyecto_id INT NOT NULL,
  tarea_id INT NULL, -- NULL: conversación del proyecto; con valor: de la tarea.
  autor_id INT NOT NULL,
  contenido TEXT NOT NULL,
  fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  fecha_actualizacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6),
  KEY idx_comentarios_proyecto_fecha (proyecto_id, fecha_creacion, id),
  KEY idx_comentarios_tarea_fecha (tarea_id, fecha_creacion, id),
  CONSTRAINT chk_comentarios_contenido CHECK (CHAR_LENGTH(TRIM(contenido)) > 0),
  CONSTRAINT fk_comentarios_proyecto FOREIGN KEY (proyecto_id)
    REFERENCES proyectos(id) ON DELETE RESTRICT,
  CONSTRAINT fk_comentarios_tarea FOREIGN KEY (proyecto_id, tarea_id)
    REFERENCES tareas(proyecto_id, id) ON DELETE RESTRICT,
  CONSTRAINT fk_comentarios_autor FOREIGN KEY (proyecto_id, autor_id)
    REFERENCES miembros_proyecto(proyecto_id, usuario_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- Mismos nombres y tipos básicos que espera NotificacionRepository.
CREATE TABLE notificaciones (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NOT NULL,
  tarea_id INT NOT NULL,
  mensaje VARCHAR(500) NOT NULL,
  leida BOOLEAN NOT NULL DEFAULT FALSE,
  fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  fecha_lectura DATETIME(6) NULL,
  KEY idx_notificaciones_fecha (usuario_id, fecha_creacion, id),
  KEY idx_notificaciones_sin_leer (usuario_id, leida, fecha_creacion, id),
  CONSTRAINT fk_notificaciones_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuarios(id) ON DELETE RESTRICT,
  CONSTRAINT fk_notificaciones_tarea FOREIGN KEY (tarea_id)
    REFERENCES tareas(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- El backend debe guardar la tarea y llamar a
-- NotificacionService.notificarAsignacion(id, titulo, responsableAnterior,
-- responsableNuevo) dentro de la MISMA transacción y tras validar permisos.
-- No agregar un trigger equivalente: causaría avisos duplicados.
-- Las contraseñas se hashean en el backend, nunca se guardan en texto plano.
