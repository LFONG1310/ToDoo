package com.todoo.comentarios;

import com.todoo.notificaciones.SesionNotificaciones;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ComentarioWebConfig implements WebMvcConfigurer {
    private final SesionNotificaciones sesion;

    public ComentarioWebConfig(SesionNotificaciones sesion) {
        this.sesion = sesion;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sesion).addPathPatterns("/api/tareas/*/comentarios");
    }
}