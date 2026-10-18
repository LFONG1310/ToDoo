package com.todoo.tableros;

/** Tablero visto por uno de sus miembros; mi_rol es el rol de quien consulta. */
public record Tablero(
        long id,
        String nombre,
        String descripcion,
        String mi_rol,
        int total_miembros) {
}
