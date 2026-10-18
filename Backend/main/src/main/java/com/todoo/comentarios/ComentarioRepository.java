package com.todoo.comentarios;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

// Tabla "comentarios" de database/propuesta_mysql.sql. La conversacion del proyecto son las filas
// con tarea_id NULL; las de una tarea (tarea_id con valor) quedan para cuando exista la API de tareas.
@Repository
public class ComentarioRepository {
    private static final String SELECT = "SELECT c.id, c.proyecto_id, c.autor_id, u.nombre AS autor_nombre,"
            + " c.contenido, c.fecha_creacion, c.fecha_actualizacion"
            + " FROM comentarios c JOIN usuarios u ON u.id = c.autor_id"
            + " WHERE c.tarea_id IS NULL AND c.proyecto_id = ?";
    private static final RowMapper<Comentario> MAPPER = (rs, row) -> {
        LocalDateTime creacion = rs.getObject("fecha_creacion", LocalDateTime.class);
        LocalDateTime actualizacion = rs.getObject("fecha_actualizacion", LocalDateTime.class);
        return new Comentario(
                rs.getLong("id"), rs.getLong("proyecto_id"), rs.getLong("autor_id"),
                rs.getString("autor_nombre"), rs.getString("contenido"),
                creacion.toInstant(ZoneOffset.UTC),
                actualizacion != null && actualizacion.isAfter(creacion) ? actualizacion.toInstant(ZoneOffset.UTC) : null);
    };
    private final JdbcTemplate jdbc;

    public ComentarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Los ultimos {@code limite} comentarios del proyecto, del mas antiguo al mas reciente. */
    public List<Comentario> listarRecientes(long proyectoId, int limite) {
        List<Comentario> recientes = new ArrayList<>(jdbc.query(SELECT
                + " ORDER BY c.fecha_creacion DESC, c.id DESC LIMIT ?", MAPPER, proyectoId, limite));
        Collections.reverse(recientes);
        return recientes;
    }

    public Optional<Comentario> buscar(long proyectoId, long comentarioId) {
        return jdbc.query(SELECT + " AND c.id = ?", MAPPER, proyectoId, comentarioId).stream().findFirst();
    }

    public long crear(long proyectoId, long autorId, String contenido, LocalDateTime fecha) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement statement = con.prepareStatement(
                    "INSERT INTO comentarios (proyecto_id, tarea_id, autor_id, contenido, fecha_creacion, fecha_actualizacion)"
                            + " VALUES (?, NULL, ?, ?, ?, ?)",
                    new String[] {"id"});
            statement.setLong(1, proyectoId);
            statement.setLong(2, autorId);
            statement.setString(3, contenido);
            statement.setObject(4, fecha);
            statement.setObject(5, fecha);
            return statement;
        }, keys);
        return keys.getKey().longValue();
    }

    public void editar(long comentarioId, String contenido, LocalDateTime fecha) {
        jdbc.update("UPDATE comentarios SET contenido = ?, fecha_actualizacion = ? WHERE id = ?", contenido, fecha, comentarioId);
    }

    public void eliminar(long comentarioId) {
        jdbc.update("DELETE FROM comentarios WHERE id = ?", comentarioId);
    }
}
