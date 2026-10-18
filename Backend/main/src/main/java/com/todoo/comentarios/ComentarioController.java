package com.todoo.comentarios;

import com.todoo.seguridad.SesionUsuario;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proyectos/{proyectoId}/comentarios")
public class ComentarioController {
    private final ComentarioService service;

    public ComentarioController(ComentarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<Comentario> listar(@PathVariable long proyectoId,
                                   @RequestAttribute(SesionUsuario.USUARIO_ID) long usuarioId) {
        return service.listar(proyectoId, usuarioId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Comentario crear(@PathVariable long proyectoId,
                            @RequestBody NuevoContenido body,
                            @RequestAttribute(SesionUsuario.USUARIO_ID) long usuarioId) {
        return service.crear(proyectoId, usuarioId, body.contenido());
    }

    @PatchMapping("/{comentarioId}")
    public Comentario editar(@PathVariable long proyectoId,
                             @PathVariable long comentarioId,
                             @RequestBody NuevoContenido body,
                             @RequestAttribute(SesionUsuario.USUARIO_ID) long usuarioId) {
        return service.editar(proyectoId, comentarioId, usuarioId, body.contenido());
    }

    @DeleteMapping("/{comentarioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long proyectoId,
                         @PathVariable long comentarioId,
                         @RequestAttribute(SesionUsuario.USUARIO_ID) long usuarioId) {
        service.eliminar(proyectoId, comentarioId, usuarioId);
    }

    public record NuevoContenido(String contenido) {
    }
}
