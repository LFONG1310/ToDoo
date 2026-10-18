package com.todoo.proyectos;

/** Proyecto (tablero) visto por uno de sus miembros; mi_rol es el rol de quien consulta. */
public record Proyecto(
        long id,
        String nombre,
        String descripcion,
        String mi_rol,
        int total_miembros) {
}
