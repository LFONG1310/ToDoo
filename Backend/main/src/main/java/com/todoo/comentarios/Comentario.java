package com.todoo.comentarios;

import java.time.Instant;

/** Comentario de la conversacion de un tablero. fecha_edicion es null si nunca se edito. */
public record Comentario(
        long id,
        long tablero_id,
        long usuario_id,
        String autor_nombre,
        String texto,
        Instant fecha_creacion,
        Instant fecha_edicion) {
}
