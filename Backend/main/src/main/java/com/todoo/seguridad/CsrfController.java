package com.todoo.seguridad;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Entrega el token que el frontend debe enviar en X-CSRF-TOKEN al crear, editar o eliminar. */
@RestController
public class CsrfController {
    private final SesionUsuario sesion;

    public CsrfController(SesionUsuario sesion) {
        this.sesion = sesion;
    }

    @GetMapping("/api/csrf")
    public Map<String, String> csrf(HttpSession session) {
        return Map.of("token", sesion.csrfToken(session));
    }
}
