package com.todoo.proyectos;

import java.util.List;

public record ProyectoDetalle(
        long id,
        String nombre,
        String descripcion,
        String mi_rol,
        List<Miembro> miembros) {
}
