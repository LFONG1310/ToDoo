package com.todoo.comentarios;

import java.time.Instant;

public record Comentario(
        long id,
        long tarea_id,
        long usuario_id,
        String texto,
        Instant fecha_creacion) {
}