package com.todoo.autenticacion;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepository {
    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Solo para validar el inicio de sesion: incluye el hash. */
    public Optional<Credencial> buscarCredencial(String correo) {
        return jdbc.query("SELECT id, nombre, correo, password_hash, activo FROM usuarios WHERE correo = ?",
                (rs, row) -> new Credencial(
                        new Usuario(rs.getLong("id"), rs.getString("nombre"), rs.getString("correo")),
                        rs.getString("password_hash"), rs.getBoolean("activo")),
                correo).stream().findFirst();
    }

    public Optional<Usuario> buscarActivo(long id) {
        return jdbc.query("SELECT id, nombre, correo FROM usuarios WHERE id = ? AND activo = TRUE",
                (rs, row) -> new Usuario(rs.getLong("id"), rs.getString("nombre"), rs.getString("correo")),
                id).stream().findFirst();
    }

    public record Credencial(Usuario usuario, String contrasenaHash, boolean activo) {
    }
}
