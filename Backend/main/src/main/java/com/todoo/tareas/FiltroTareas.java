package com.todoo.tareas;

import java.util.Set;

// Criterios opcionales del tablero; null significa "sin filtrar por ese campo".
public record FiltroTareas(String prioridad, Long responsableId) {
    public static final Set<String> PRIORIDADES = Set.of("alta", "media", "baja");

    public FiltroTareas {
        if (prioridad != null && !PRIORIDADES.contains(prioridad)) {
            throw new IllegalArgumentException("Prioridad invalida.");
        }
        if (responsableId != null && responsableId <= 0) {
            throw new IllegalArgumentException("Responsable invalido.");
        }
    }
}
