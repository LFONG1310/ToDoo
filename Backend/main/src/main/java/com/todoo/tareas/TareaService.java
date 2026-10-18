package com.todoo.tareas;

import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TareaService {
    private final TareaRepository repository;

    public TareaService(TareaRepository repository) {
        this.repository = repository;
    }

    // "Solo mis tareas" usa siempre el usuario de la sesion, nunca un id enviado por el navegador.
    @Transactional(readOnly = true)
    public List<Tarea> filtrar(long proyectoId, long usuarioId, String prioridad, Long responsableId, boolean soloMias) {
        validarAcceso(proyectoId, usuarioId);
        if (soloMias && responsableId != null && !Objects.equals(responsableId, usuarioId)) {
            return List.of();
        }
        Long responsable = soloMias ? Long.valueOf(usuarioId) : responsableId;
        return repository.buscar(proyectoId, new FiltroTareas(prioridad, responsable));
    }

    @Transactional(readOnly = true)
    public List<Responsable> responsables(long proyectoId, long usuarioId) {
        validarAcceso(proyectoId, usuarioId);
        return repository.responsables(proyectoId);
    }

    private void validarAcceso(long proyectoId, long usuarioId) {
        // 404 en lugar de 403 para no revelar que proyectos existen.
        if (proyectoId <= 0 || !repository.esMiembroActivo(proyectoId, usuarioId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado.");
        }
    }
}
