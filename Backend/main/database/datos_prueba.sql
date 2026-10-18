-- Datos SOLO para desarrollo local. No ejecutar en produccion.
-- Ejecutar en HeidiSQL despues de propuesta_mysql.sql, sobre la base todoo.
-- Los tres usuarios tienen la contrasena: ToDoo2026!

USE todoo;

INSERT INTO usuarios (id, nombre, correo, password_hash) VALUES
    (1, 'Luis Fong', 'luis@todoo.dev', 'pbkdf2_sha256$600000$jVjiMi5GnmNR73QiMyWSOQ==$A8ppEdx4HblaFPrwMBU3XUKn/EmCYo70VXJIz7PXynQ='),
    (2, 'Diego', 'diego@todoo.dev', 'pbkdf2_sha256$600000$1JHnoP9N5xK9w3KCEvyGGw==$X2rQ+fvrwY5j+ktFAlBeGvlg8HgvM8rYelgMv7KizQE='),
    (3, 'Andrea', 'andrea@todoo.dev', 'pbkdf2_sha256$600000$ByRYulzaf9K6plXGKQzTSg==$TMXrlkZX1zz2njlmJUxoiLZzh7dSUkrrr3JotWVVn2E=');

-- El proyecto 1 es el que muestra el tablero (/kanban) del frontend: PROYECTO_PRINCIPAL.apiId = 1.
INSERT INTO proyectos (id, nombre, descripcion, creado_por) VALUES
    (1, 'Mi tablero de tareas', 'Organiza tus tareas y sigue su avance desde Por hacer hasta Hecho.', 1),
    (2, 'Diseno', 'Pantallas y estilos', 2);

-- Andrea no es miembro del proyecto 2: no puede ver ni escribir en su conversacion.
INSERT INTO miembros_proyecto (proyecto_id, usuario_id, rol) VALUES
    (1, 1, 'administrador'),
    (1, 2, 'miembro'),
    (1, 3, 'miembro'),
    (2, 2, 'administrador'),
    (2, 1, 'miembro');

-- Conversacion del proyecto 1 (tarea_id NULL). El backend guarda las fechas en UTC.
INSERT INTO comentarios (proyecto_id, tarea_id, autor_id, contenido, fecha_creacion, fecha_actualizacion) VALUES
    (1, NULL, 1, 'Bienvenidos al tablero. Aqui coordinamos el avance del proyecto.',
        UTC_TIMESTAMP(6) - INTERVAL 1 HOUR, UTC_TIMESTAMP(6) - INTERVAL 1 HOUR),
    (1, NULL, 2, 'Listo, ya tengo las notificaciones conectadas.',
        UTC_TIMESTAMP(6) - INTERVAL 30 MINUTE, UTC_TIMESTAMP(6) - INTERVAL 30 MINUTE);
