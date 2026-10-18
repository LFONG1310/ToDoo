package com.todoo;

import com.todoo.seguridad.ContrasenaHasher;
import com.todoo.seguridad.SesionUsuario;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.mock.web.MockHttpSession;

/** Ayudas para preparar usuarios, proyectos y sesiones con las tablas de propuesta_mysql.sql. */
public final class DatosPrueba {
    public static final String CONTRASENA = "Secreta123!";
    private static String hashCache;
    private final JdbcTemplate jdbc;
    private final SesionUsuario sesion;

    public DatosPrueba(JdbcTemplate jdbc, SesionUsuario sesion, ContrasenaHasher hasher) {
        this.jdbc = jdbc;
        this.sesion = sesion;
        synchronized (DatosPrueba.class) {
            if (hashCache == null) {
                hashCache = hasher.hashear(CONTRASENA);
            }
        }
    }

    public void limpiar() {
        jdbc.update("DELETE FROM notificaciones");
        jdbc.update("DELETE FROM comentarios");
        jdbc.update("DELETE FROM tareas");
        jdbc.update("DELETE FROM miembros_proyecto");
        jdbc.update("DELETE FROM proyectos");
        jdbc.update("DELETE FROM usuarios");
    }

    public long usuario(String nombre, String correo) {
        return insertar("INSERT INTO usuarios (nombre, correo, password_hash) VALUES (?, ?, ?)", nombre, correo, hashCache);
    }

    /** Crea el proyecto con su creador como administrador. */
    public long proyecto(String nombre, long administrador) {
        long id = insertar("INSERT INTO proyectos (nombre, descripcion, creado_por) VALUES (?, ?, ?)",
                nombre, "Descripcion de " + nombre, administrador);
        miembro(id, administrador, "administrador");
        return id;
    }

    public void miembro(long proyectoId, long usuarioId, String rol) {
        jdbc.update("INSERT INTO miembros_proyecto (proyecto_id, usuario_id, rol) VALUES (?, ?, ?)", proyectoId, usuarioId, rol);
    }

    public long tarea(long proyectoId, long responsableId, String titulo) {
        return insertar("INSERT INTO tareas (proyecto_id, titulo, descripcion, responsable_id, creado_por, fecha_limite)"
                + " VALUES (?, ?, '', ?, ?, ?)", proyectoId, titulo, responsableId, responsableId, LocalDateTime.of(2026, 12, 1, 12, 0));
    }

    public long comentario(long proyectoId, long autorId, String contenido, LocalDateTime fecha) {
        return insertar("INSERT INTO comentarios (proyecto_id, autor_id, contenido, fecha_creacion, fecha_actualizacion)"
                + " VALUES (?, ?, ?, ?, ?)", proyectoId, autorId, contenido, fecha, fecha);
    }

    public long comentarioDeTarea(long proyectoId, long tareaId, long autorId, String contenido, LocalDateTime fecha) {
        return insertar("INSERT INTO comentarios (proyecto_id, tarea_id, autor_id, contenido, fecha_creacion, fecha_actualizacion)"
                + " VALUES (?, ?, ?, ?, ?, ?)", proyectoId, tareaId, autorId, contenido, fecha, fecha);
    }

    public MockHttpSession login(long usuarioId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SesionUsuario.USUARIO_ID, usuarioId);
        return session;
    }

    public String csrf(MockHttpSession session) {
        return sesion.csrfToken(session);
    }

    private long insertar(String sql, Object... valores) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var statement = con.prepareStatement(sql, new String[] {"id"});
            for (int i = 0; i < valores.length; i++) {
                statement.setObject(i + 1, valores[i]);
            }
            return statement;
        }, keys);
        return keys.getKey().longValue();
    }
}
