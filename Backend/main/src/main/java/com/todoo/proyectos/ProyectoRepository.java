package com.todoo.proyectos;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

// SQL basado en database/propuesta_mysql.sql (proyectos, miembros_proyecto, usuarios).
// Solo cuentan los miembros activos, igual que TareaRepository.
@Repository
public class ProyectoRepository {
    private static final String SELECT_PROYECTO = "SELECT p.id, p.nombre, p.descripcion, m.rol AS mi_rol,"
            + " (SELECT COUNT(*) FROM miembros_proyecto x JOIN usuarios u ON u.id = x.usuario_id"
            + "   WHERE x.proyecto_id = p.id AND x.activo = TRUE AND u.activo = TRUE) AS total_miembros"
            + " FROM proyectos p JOIN miembros_proyecto m"
            + "   ON m.proyecto_id = p.id AND m.usuario_id = ? AND m.activo = TRUE";
    private static final RowMapper<Proyecto> MAPPER = (rs, row) -> new Proyecto(
            rs.getLong("id"), rs.getString("nombre"), rs.getString("descripcion"),
            rs.getString("mi_rol"), rs.getInt("total_miembros"));
    private final JdbcTemplate jdbc;

    public ProyectoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Proyecto> listarDeUsuario(long usuarioId) {
        return jdbc.query(SELECT_PROYECTO + " WHERE p.archivado = FALSE ORDER BY p.nombre ASC, p.id ASC",
                MAPPER, usuarioId);
    }

    public Optional<Proyecto> buscarParaMiembro(long proyectoId, long usuarioId) {
        return jdbc.query(SELECT_PROYECTO + " WHERE p.id = ?", MAPPER, usuarioId, proyectoId).stream().findFirst();
    }

    /** Rol del usuario si es miembro activo del proyecto. */
    public Optional<String> rolDe(long proyectoId, long usuarioId) {
        return jdbc.queryForList("SELECT rol FROM miembros_proyecto"
                        + " WHERE proyecto_id = ? AND usuario_id = ? AND activo = TRUE",
                String.class, proyectoId, usuarioId).stream().findFirst();
    }

    public List<Miembro> listarMiembros(long proyectoId) {
        return jdbc.query("SELECT u.id, u.nombre, m.rol FROM miembros_proyecto m"
                        + " JOIN usuarios u ON u.id = m.usuario_id"
                        + " WHERE m.proyecto_id = ? AND m.activo = TRUE AND u.activo = TRUE"
                        + " ORDER BY CASE WHEN m.rol = 'administrador' THEN 0 ELSE 1 END, u.nombre ASC, u.id ASC",
                (rs, row) -> new Miembro(rs.getLong("id"), rs.getString("nombre"), rs.getString("rol")),
                proyectoId);
    }
}
