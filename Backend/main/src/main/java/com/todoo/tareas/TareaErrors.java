package com.todoo.tareas;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = TareaController.class)
public class TareaErrors {
    private static final Logger LOGGER = LoggerFactory.getLogger(TareaErrors.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> status(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(
                Map.of("mensaje", exception.getReason() == null ? "No se pudo completar la solicitud." : exception.getReason()));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> invalidParameter() {
        return ResponseEntity.badRequest().body(Map.of("mensaje", "Los filtros de la solicitud no son validos."));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> database(DataAccessException exception) {
        LOGGER.error("Fallo al consultar las tareas: {}", exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                Map.of("mensaje", "No se pudieron consultar las tareas. Intenta de nuevo."));
    }
}
