package com.todoo;

import com.todoo.seguridad.ContrasenaHasher;
import com.todoo.seguridad.SesionUsuario;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.mock.web.MockHttpSession;

/** Ayudas para preparar usuarios, tableros y sesiones en las pruebas de integracion. */
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
        jdbc.update("DELETE FROM comentarios_tablero");
        jdbc.update("DELETE FROM tablero_miembros");
        jdbc.update("DELETE FROM tableros");
        jdbc.update("DELETE FROM usuarios");
    }

    public long usuario(String nombre, String correo) {
        return insertar("INSERT INTO usuarios (nombre, correo, contrasena_hash) VALUES (?, ?, ?)", nombre, correo, hashCache);
    }

    public long tablero(String nombre, long propietario) {
        long id = insertar("INSERT INTO tableros (nombre, descripcion, creado_por) VALUES (?, ?, ?)",
                nombre, "Descripcion de " + nombre, propietario);
        miembro(id, propietario, "PROPIETARIO");
        return id;
    }

    public void miembro(long tableroId, long usuarioId, String rol) {
        jdbc.update("INSERT INTO tablero_miembros (tablero_id, usuario_id, rol) VALUES (?, ?, ?)", tableroId, usuarioId, rol);
    }

    public long comentario(long tableroId, long usuarioId, String texto, LocalDateTime fecha) {
        return insertar("INSERT INTO comentarios_tablero (tablero_id, usuario_id, texto, fecha_creacion) VALUES (?, ?, ?, ?)",
                tableroId, usuarioId, texto, fecha);
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
