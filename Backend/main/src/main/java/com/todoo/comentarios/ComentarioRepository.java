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

@Repository
public class ComentarioRepository {
    private static final String SELECT = "SELECT c.id, c.tablero_id, c.usuario_id, u.nombre AS autor_nombre,"
            + " c.texto, c.fecha_creacion, c.fecha_edicion"
            + " FROM comentarios_tablero c JOIN usuarios u ON u.id = c.usuario_id";
    private static final RowMapper<Comentario> MAPPER = (rs, row) -> {
        LocalDateTime edicion = rs.getObject("fecha_edicion", LocalDateTime.class);
        return new Comentario(
                rs.getLong("id"), rs.getLong("tablero_id"), rs.getLong("usuario_id"),
                rs.getString("autor_nombre"), rs.getString("texto"),
                rs.getObject("fecha_creacion", LocalDateTime.class).toInstant(ZoneOffset.UTC),
                edicion == null ? null : edicion.toInstant(ZoneOffset.UTC));
    };
    private final JdbcTemplate jdbc;

    public ComentarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Los ultimos {@code limite} comentarios del tablero, del mas antiguo al mas reciente. */
    public List<Comentario> listarRecientes(long tableroId, int limite) {
        List<Comentario> recientes = new ArrayList<>(jdbc.query(SELECT + " WHERE c.tablero_id = ?"
                + " ORDER BY c.fecha_creacion DESC, c.id DESC LIMIT ?", MAPPER, tableroId, limite));
        Collections.reverse(recientes);
        return recientes;
    }

    public Optional<Comentario> buscar(long tableroId, long comentarioId) {
        return jdbc.query(SELECT + " WHERE c.tablero_id = ? AND c.id = ?", MAPPER, tableroId, comentarioId)
                .stream().findFirst();
    }

    public long crear(long tableroId, long usuarioId, String texto, LocalDateTime fecha) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement statement = con.prepareStatement(
                    "INSERT INTO comentarios_tablero (tablero_id, usuario_id, texto, fecha_creacion) VALUES (?, ?, ?, ?)",
                    new String[] {"id"});
            statement.setLong(1, tableroId);
            statement.setLong(2, usuarioId);
            statement.setString(3, texto);
            statement.setObject(4, fecha);
            return statement;
        }, keys);
        return keys.getKey().longValue();
    }

    public void editar(long comentarioId, String texto, LocalDateTime fecha) {
        jdbc.update("UPDATE comentarios_tablero SET texto = ?, fecha_edicion = ? WHERE id = ?", texto, fecha, comentarioId);
    }

    public void eliminar(long comentarioId) {
        jdbc.update("DELETE FROM comentarios_tablero WHERE id = ?", comentarioId);
    }
}
