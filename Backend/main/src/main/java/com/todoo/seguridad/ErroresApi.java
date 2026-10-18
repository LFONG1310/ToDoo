package com.todoo.seguridad;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

/** Respuestas de error en JSON ({"mensaje": ...}) sin exponer detalles internos. */
@RestControllerAdvice(basePackages = {"com.todoo.seguridad", "com.todoo.autenticacion", "com.todoo.tableros", "com.todoo.comentarios"})
public class ErroresApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErroresApi.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> status(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(
                Map.of("mensaje", exception.getReason() == null ? "No se pudo completar la solicitud." : exception.getReason()));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, String>> solicitudInvalida() {
        return ResponseEntity.badRequest().body(Map.of("mensaje", "Los datos de la solicitud no son validos."));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> baseDeDatos(DataAccessException exception) {
        LOGGER.error("Fallo al acceder a la base de datos: {}", exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                Map.of("mensaje", "No se pudo conectar con la base de datos. Intenta de nuevo."));
    }
}
