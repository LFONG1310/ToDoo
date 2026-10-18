package com.todoo.comentarios;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ComentarioService {
    private static final int MAX_TEXTO = 1000;
    private final ComentarioRepository repository;
    private final Clock clock;

    public ComentarioService(ComentarioRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Comentario> listar(long tareaId) {
        validarTarea(tareaId);
        return repository.listarPorTarea(tareaId);
    }

    @Transactional
    public void crear(long tareaId, long usuarioId, String texto) {
        validarTarea(tareaId);
        if (texto == null || texto.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El comentario no puede estar vacio.");
        }
        String limpio = texto.strip();
        if (limpio.codePointCount(0, limpio.length()) > MAX_TEXTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El comentario es demasiado largo.");
        }
        repository.crear(tareaId, usuarioId, limpio, LocalDateTime.now(clock));
    }

    private void validarTarea(long tareaId) {
        if (tareaId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador de tarea invalido.");
        }
    }
}