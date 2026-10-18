package com.todoo.comentarios;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tareas/{tareaId}/comentarios")
public class ComentarioController {
    private final ComentarioService service;

    public ComentarioController(ComentarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<Comentario> listar(@PathVariable long tareaId) {
        return service.listar(tareaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void crear(@PathVariable long tareaId,
                      @RequestBody NuevoComentario body,
                      @RequestAttribute("usuarioId") long usuarioId) {
        service.crear(tareaId, usuarioId, body.texto());
    }

    public record NuevoComentario(String texto) {
    }
}