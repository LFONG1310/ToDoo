package com.todoo.comentarios;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ComentarioRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<Comentario> MAPPER = (rs, row) -> new Comentario(
            rs.getLong("id"), rs.getLong("tarea_id"), rs.getLong("usuario_id"),
            rs.getString("texto"),
            rs.getObject("fecha_creacion", LocalDateTime.class).toInstant(ZoneOffset.UTC));

    public ComentarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Comentario> listarPorTarea(long tareaId) {
        return jdbc.query("SELECT * FROM comentarios WHERE tarea_id = ?"
                + " ORDER BY fecha_creacion ASC, id ASC", MAPPER, tareaId);
    }

    public void crear(long tareaId, long usuarioId, String texto, LocalDateTime fecha) {
        jdbc.update("INSERT INTO comentarios (tarea_id, usuario_id, texto, fecha_creacion)"
                + " VALUES (?, ?, ?, ?)", tareaId, usuarioId, texto, fecha);
    }
}