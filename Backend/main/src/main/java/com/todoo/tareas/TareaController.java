package com.todoo.tareas;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proyectos/{proyectoId}")
public class TareaController {
    private final TareaService service;

    public TareaController(TareaService service) {
        this.service = service;
    }

    // GET /api/proyectos/1/tareas?prioridad=alta&responsableId=3&soloMias=true
    @GetMapping("/tareas")
    public List<Tarea> listar(
            @PathVariable long proyectoId,
            @RequestAttribute("usuarioId") long usuarioId,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) Long responsableId,
            @RequestParam(defaultValue = "false") boolean soloMias) {
        return service.filtrar(proyectoId, usuarioId, prioridad, responsableId, soloMias);
    }

    // Opciones para el selector "Responsable" del tablero.
    @GetMapping("/responsables")
    public List<Responsable> responsables(@PathVariable long proyectoId, @RequestAttribute("usuarioId") long usuarioId) {
        return service.responsables(proyectoId, usuarioId);
    }
}
