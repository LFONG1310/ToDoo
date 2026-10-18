package com.todoo.comentarios;

import java.time.Instant;

/**
 * Comentario de la conversacion de un proyecto (tablero).
 * fecha_edicion es null si el comentario nunca se edito.
 */
public record Comentario(
        long id,
        long proyecto_id,
        long autor_id,
        String autor_nombre,
        String contenido,
        Instant fecha_creacion,
        Instant fecha_edicion) {
}
