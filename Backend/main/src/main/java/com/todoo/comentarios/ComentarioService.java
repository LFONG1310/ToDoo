package com.todoo.comentarios;

import com.todoo.tableros.TableroService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Conversacion de cada tablero. Reglas:
 * - Solo los miembros del tablero pueden leer y escribir.
 * - Solo el autor puede editar su comentario.
 * - El autor o el PROPIETARIO del tablero pueden eliminarlo.
 */
@Service
public class ComentarioService {
    public static final int MAX_TEXTO = 1000;
    static final int LIMITE_LISTA = 200;
    private final ComentarioRepository repository;
    private final TableroService tableros;
    private final Clock clock;

    public ComentarioService(ComentarioRepository repository, TableroService tableros, Clock clock) {
        this.repository = repository;
        this.tableros = tableros;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Comentario> listar(long tableroId, long usuarioId) {
        tableros.exigirMiembro(tableroId, usuarioId);
        return repository.listarRecientes(tableroId, LIMITE_LISTA);
    }

    @Transactional
    public Comentario crear(long tableroId, long usuarioId, String texto) {
        tableros.exigirMiembro(tableroId, usuarioId);
        long id = repository.crear(tableroId, usuarioId, validar(texto), LocalDateTime.now(clock));
        return buscar(tableroId, id);
    }

    @Transactional
    public Comentario editar(long tableroId, long comentarioId, long usuarioId, String texto) {
        tableros.exigirMiembro(tableroId, usuarioId);
        Comentario actual = buscar(tableroId, comentarioId);
        if (actual.usuario_id() != usuarioId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor puede editar este comentario.");
        }
        String limpio = validar(texto);
        if (limpio.equals(actual.texto())) {
            return actual;
        }
        repository.editar(comentarioId, limpio, LocalDateTime.now(clock));
        return buscar(tableroId, comentarioId);
    }

    @Transactional
    public void eliminar(long tableroId, long comentarioId, long usuarioId) {
        String rol = tableros.exigirMiembro(tableroId, usuarioId);
        Comentario actual = buscar(tableroId, comentarioId);
        if (actual.usuario_id() != usuarioId && !TableroService.PROPIETARIO.equals(rol)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el autor o el propietario del tablero pueden eliminar este comentario.");
        }
        repository.eliminar(comentarioId);
    }

    private Comentario buscar(long tableroId, long comentarioId) {
        return repository.buscar(tableroId, comentarioId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario no encontrado."));
    }

    private static String validar(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El comentario no puede estar vacio.");
        }
        String limpio = texto.strip();
        if (limpio.codePointCount(0, limpio.length()) > MAX_TEXTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El comentario es demasiado largo.");
        }
        return limpio;
    }
}
