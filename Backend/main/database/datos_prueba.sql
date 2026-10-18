-- Datos SOLO para desarrollo local. No ejecutar en produccion.
-- Los tres usuarios tienen la contrasena: ToDoo2026!

INSERT INTO usuarios (id, nombre, correo, contrasena_hash) VALUES
    (1, 'Luis Fong', 'luis@todoo.dev', 'pbkdf2_sha256$600000$jVjiMi5GnmNR73QiMyWSOQ==$A8ppEdx4HblaFPrwMBU3XUKn/EmCYo70VXJIz7PXynQ='),
    (2, 'Diego', 'diego@todoo.dev', 'pbkdf2_sha256$600000$1JHnoP9N5xK9w3KCEvyGGw==$X2rQ+fvrwY5j+ktFAlBeGvlg8HgvM8rYelgMv7KizQE='),
    (3, 'Andrea', 'andrea@todoo.dev', 'pbkdf2_sha256$600000$ByRYulzaf9K6plXGKQzTSg==$TMXrlkZX1zz2njlmJUxoiLZzh7dSUkrrr3JotWVVn2E=');

INSERT INTO tableros (id, nombre, descripcion, creado_por) VALUES
    (1, 'Proyecto final', 'Tablero principal del equipo', 1),
    (2, 'Diseno', 'Pantallas y estilos', 2);

-- Andrea no es miembro del tablero 2: no puede ver ni escribir en su conversacion.
INSERT INTO tablero_miembros (tablero_id, usuario_id, rol) VALUES
    (1, 1, 'PROPIETARIO'),
    (1, 2, 'RESPONSABLE'),
    (1, 3, 'RESPONSABLE'),
    (2, 2, 'PROPIETARIO'),
    (2, 1, 'RESPONSABLE');

-- El backend guarda todas las fechas en UTC; por eso se usa UTC_TIMESTAMP y no NOW().
INSERT INTO comentarios_tablero (tablero_id, usuario_id, texto, fecha_creacion) VALUES
    (1, 1, 'Bienvenidos al tablero. Aqui coordinamos el avance del proyecto.', UTC_TIMESTAMP(6) - INTERVAL 1 HOUR),
    (1, 2, 'Listo, ya tengo las notificaciones conectadas.', UTC_TIMESTAMP(6) - INTERVAL 30 MINUTE);
