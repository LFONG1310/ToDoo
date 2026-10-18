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
@RequestMapping("/api/tableros/{tableroId}/comentarios")
public class ComentarioController {
    private final ComentarioService service;

    public ComentarioController(ComentarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<Comentario> listar(@PathVariable long tableroId,
                                   @RequestAttribute(SesionUsuario.USUARIO_ID) long usuarioId) {
        return service.listar(tableroId, usuarioId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Comentario crear(@PathVariable long tableroId,
                            @RequestBody TextoComentario body,
                            @RequestAttribute(SesionUsuario.USUARIO_ID) long usuarioId) {
        return service.crear(tableroId, usuarioId, body.texto());
    }

    @PatchMapping("/{comentarioId}")
    public Comentario editar(@PathVariable long tableroId,
                             @PathVariable long comentarioId,
                             @RequestBody TextoComentario body,
                             @RequestAttribute(SesionUsuario.USUARIO_ID) long usuarioId) {
        return service.editar(tableroId, comentarioId, usuarioId, body.texto());
    }

    @DeleteMapping("/{comentarioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long tableroId,
                         @PathVariable long comentarioId,
                         @RequestAttribute(SesionUsuario.USUARIO_ID) long usuarioId) {
        service.eliminar(tableroId, comentarioId, usuarioId);
    }

    public record TextoComentario(String texto) {
    }
}
