package com.todoo.seguridad;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Protege toda la API con la sesion; solo el inicio de sesion queda abierto. */
@Configuration
public class SeguridadWebConfig implements WebMvcConfigurer {
    private final SesionUsuario sesion;

    public SeguridadWebConfig(SesionUsuario sesion) {
        this.sesion = sesion;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sesion)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }
}
