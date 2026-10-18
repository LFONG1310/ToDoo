package com.todoo.tareas;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

// Todo el SQL del modulo vive aqui: si el esquema definitivo cambia nombres de
// tablas o columnas, solo este archivo necesita ajustarse.
// Basado en database/propuesta_mysql.sql (tareas, miembros_proyecto, usuarios).
@Repository
public class TareaRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<Tarea> MAPPER = (rs, row) -> new Tarea(
            rs.getLong("id"), rs.getLong("proyecto_id"), rs.getString("titulo"),
            rs.getString("descripcion"), rs.getString("prioridad"), rs.getString("estado"),
            rs.getInt("posicion"),
            new Responsable(rs.getLong("responsable_id"), rs.getString("responsable_nombre")),
            rs.getObject("fecha_limite", LocalDateTime.class).toInstant(ZoneOffset.UTC));

    public TareaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean esMiembroActivo(long proyectoId, long usuarioId) {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM miembros_proyecto"
                + " WHERE proyecto_id = ? AND usuario_id = ? AND activo = TRUE",
                Integer.class, proyectoId, usuarioId);
        return total != null && total > 0;
    }

    public List<Tarea> buscar(long proyectoId, FiltroTareas filtro) {
        StringBuilder sql = new StringBuilder("SELECT t.id, t.proyecto_id, t.titulo, t.descripcion,"
                + " t.prioridad, t.estado, t.posicion, t.responsable_id, t.fecha_limite,"
                + " u.nombre AS responsable_nombre"
                + " FROM tareas t JOIN usuarios u ON u.id = t.responsable_id"
                + " WHERE t.proyecto_id = ? AND t.archivada = FALSE");
        List<Object> params = new ArrayList<>(List.of(proyectoId));
        if (filtro.prioridad() != null) {
            sql.append(" AND t.prioridad = ?");
            params.add(filtro.prioridad());
        }
        if (filtro.responsableId() != null) {
            sql.append(" AND t.responsable_id = ?");
            params.add(filtro.responsableId());
        }
        sql.append(" ORDER BY t.estado, t.posicion, t.id");
        return jdbc.query(sql.toString(), MAPPER, params.toArray());
    }

    public List<Responsable> responsables(long proyectoId) {
        return jdbc.query("SELECT u.id, u.nombre FROM miembros_proyecto m"
                + " JOIN usuarios u ON u.id = m.usuario_id"
                + " WHERE m.proyecto_id = ? AND m.activo = TRUE AND u.activo = TRUE"
                + " ORDER BY u.nombre, u.id",
                (rs, row) -> new Responsable(rs.getLong("id"), rs.getString("nombre")), proyectoId);
    }
}
