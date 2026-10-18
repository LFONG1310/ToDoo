package com.todoo.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Exige una sesion iniciada en las rutas /api y valida el token CSRF en las peticiones que modifican datos.
 * El id del usuario se toma siempre de la sesion, nunca de lo que envie el navegador.
 */
@Component
public class SesionUsuario implements HandlerInterceptor {
    public static final String USUARIO_ID = "usuarioId";
    public static final String CSRF_HEADER = "X-CSRF-TOKEN";
    private static final String CSRF_TOKEN = "csrfToken";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        response.setHeader("Cache-Control", "no-store");
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute(USUARIO_ID);
        if (!(value instanceof Long || value instanceof Integer) || ((Number) value).longValue() <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesion para continuar.");
        }
        request.setAttribute(USUARIO_ID, ((Number) value).longValue());
        if (!"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod())) {
            String expected = (String) session.getAttribute(CSRF_TOKEN);
            String supplied = request.getHeader(CSRF_HEADER);
            if (expected == null || supplied == null || !MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Actualiza la pagina e intenta de nuevo.");
            }
        }
        return true;
    }

    public String csrfToken(HttpSession session) {
        synchronized (session) {
            String token = (String) session.getAttribute(CSRF_TOKEN);
            if (token == null) {
                token = UUID.randomUUID().toString();
                session.setAttribute(CSRF_TOKEN, token);
            }
            return token;
        }
    }
}
