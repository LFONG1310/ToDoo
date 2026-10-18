package com.todoo.tareas;

import java.time.Instant;

public record Tarea(
        long id,
        long proyecto_id,
        String titulo,
        String descripcion,
        String prioridad,
        String estado,
        int posicion,
        Responsable responsable,
        Instant fecha_limite) {
}
