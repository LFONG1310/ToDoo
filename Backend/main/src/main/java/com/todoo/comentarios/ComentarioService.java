package com.todoo.comentarios;

import com.todoo.proyectos.ProyectoService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Conversacion de cada proyecto (tablero). Reglas:
 * - Solo los miembros activos del proyecto pueden leer y escribir.
 * - Solo el autor puede editar su comentario.
 * - El autor o un administrador del proyecto pueden eliminarlo.
 */
@Service
public class ComentarioService {
    public static final int MAX_CONTENIDO = 1000;
    static final int LIMITE_LISTA = 200;
    private final ComentarioRepository repository;
    private final ProyectoService proyectos;
    private final Clock clock;

    public ComentarioService(ComentarioRepository repository, ProyectoService proyectos, Clock clock) {
        this.repository = repository;
        this.proyectos = proyectos;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Comentario> listar(long proyectoId, long usuarioId) {
        proyectos.exigirMiembro(proyectoId, usuarioId);
        return repository.listarRecientes(proyectoId, LIMITE_LISTA);
    }

    @Transactional
    public Comentario crear(long proyectoId, long usuarioId, String contenido) {
        proyectos.exigirMiembro(proyectoId, usuarioId);
        long id = repository.crear(proyectoId, usuarioId, validar(contenido), LocalDateTime.now(clock));
        return buscar(proyectoId, id);
    }

    @Transactional
    public Comentario editar(long proyectoId, long comentarioId, long usuarioId, String contenido) {
        proyectos.exigirMiembro(proyectoId, usuarioId);
        Comentario actual = buscar(proyectoId, comentarioId);
        if (actual.autor_id() != usuarioId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor puede editar este comentario.");
        }
        String limpio = validar(contenido);
        if (limpio.equals(actual.contenido())) {
            return actual;
        }
        repository.editar(comentarioId, limpio, LocalDateTime.now(clock));
        return buscar(proyectoId, comentarioId);
    }

    @Transactional
    public void eliminar(long proyectoId, long comentarioId, long usuarioId) {
        String rol = proyectos.exigirMiembro(proyectoId, usuarioId);
        Comentario actual = buscar(proyectoId, comentarioId);
        if (actual.autor_id() != usuarioId && !ProyectoService.ADMINISTRADOR.equals(rol)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el autor o un administrador del proyecto pueden eliminar este comentario.");
        }
        repository.eliminar(comentarioId);
    }

    private Comentario buscar(long proyectoId, long comentarioId) {
        return repository.buscar(proyectoId, comentarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario no encontrado."));
    }

    private static String validar(String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El comentario no puede estar vacio.");
        }
        String limpio = contenido.strip();
        if (limpio.codePointCount(0, limpio.length()) > MAX_CONTENIDO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El comentario es demasiado largo.");
        }
        return limpio;
    }
}
