package com.todoo.tareas;

import com.todoo.notificaciones.SesionNotificaciones;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Mismo atributo de sesion que notificaciones; el login futuro solo debe establecer "usuarioId".
// Estas rutas son de solo lectura, por eso no exigen token CSRF.
@Configuration
public class TareaWebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                response.setHeader("Cache-Control", "no-store");
                HttpSession session = request.getSession(false);
                Object value = session == null ? null : session.getAttribute(SesionNotificaciones.USUARIO_ID);
                if (!(value instanceof Long || value instanceof Integer) || ((Number) value).longValue() <= 0) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesion para consultar las tareas.");
                }
                request.setAttribute(SesionNotificaciones.USUARIO_ID, ((Number) value).longValue());
                return true;
            }
        }).addPathPatterns("/api/proyectos/*/tareas", "/api/proyectos/*/responsables");
    }
}
