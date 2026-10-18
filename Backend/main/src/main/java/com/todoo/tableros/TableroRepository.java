package com.todoo.tableros;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TableroRepository {
    private static final String SELECT_TABLERO = "SELECT t.id, t.nombre, t.descripcion, m.rol AS mi_rol,"
            + " (SELECT COUNT(*) FROM tablero_miembros x WHERE x.tablero_id = t.id) AS total_miembros"
            + " FROM tableros t JOIN tablero_miembros m ON m.tablero_id = t.id AND m.usuario_id = ?";
    private static final RowMapper<Tablero> MAPPER = (rs, row) -> new Tablero(
            rs.getLong("id"), rs.getString("nombre"), rs.getString("descripcion"),
            rs.getString("mi_rol"), rs.getInt("total_miembros"));
    private final JdbcTemplate jdbc;

    public TableroRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Tablero> listarDeUsuario(long usuarioId) {
        return jdbc.query(SELECT_TABLERO + " ORDER BY t.nombre ASC, t.id ASC", MAPPER, usuarioId);
    }

    public Optional<Tablero> buscarParaMiembro(long tableroId, long usuarioId) {
        return jdbc.query(SELECT_TABLERO + " WHERE t.id = ?", MAPPER, usuarioId, tableroId).stream().findFirst();
    }

    public Optional<String> rolDe(long tableroId, long usuarioId) {
        return jdbc.queryForList("SELECT rol FROM tablero_miembros WHERE tablero_id = ? AND usuario_id = ?",
                String.class, tableroId, usuarioId).stream().findFirst();
    }

    public List<Miembro> listarMiembros(long tableroId) {
        return jdbc.query("SELECT u.id, u.nombre, m.rol FROM tablero_miembros m"
                        + " JOIN usuarios u ON u.id = m.usuario_id"
                        + " WHERE m.tablero_id = ? AND u.activo = TRUE"
                        + " ORDER BY CASE WHEN m.rol = 'PROPIETARIO' THEN 0 ELSE 1 END, u.nombre ASC",
                (rs, row) -> new Miembro(rs.getLong("id"), rs.getString("nombre"), rs.getString("rol")),
                tableroId);
    }
}
