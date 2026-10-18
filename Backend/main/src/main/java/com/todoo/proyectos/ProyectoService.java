package com.todoo.proyectos;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProyectoService {
    public static final String ADMINISTRADOR = "administrador";
    private final ProyectoRepository repository;

    public ProyectoService(ProyectoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Proyecto> listar(long usuarioId) {
        return repository.listarDeUsuario(usuarioId);
    }

    @Transactional(readOnly = true)
    public ProyectoDetalle detalle(long proyectoId, long usuarioId) {
        Proyecto proyecto = repository.buscarParaMiembro(proyectoId, usuarioId).orElseThrow(ProyectoService::noEncontrado);
        return new ProyectoDetalle(proyecto.id(), proyecto.nombre(), proyecto.descripcion(), proyecto.mi_rol(),
                repository.listarMiembros(proyectoId));
    }

    /**
     * Devuelve el rol del usuario en el proyecto. Si no es miembro activo responde 404 (y no 403)
     * para no revelar que el proyecto existe, igual que TareaService.
     */
    @Transactional(readOnly = true)
    public String exigirMiembro(long proyectoId, long usuarioId) {
        return repository.rolDe(proyectoId, usuarioId).orElseThrow(ProyectoService::noEncontrado);
    }

    private static ResponseStatusException noEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado.");
    }
}
