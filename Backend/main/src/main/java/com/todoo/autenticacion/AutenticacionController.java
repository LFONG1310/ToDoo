package com.todoo.autenticacion;

import com.todoo.seguridad.SesionUsuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {
    private final AutenticacionService service;

    public AutenticacionController(AutenticacionService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public Usuario login(@RequestBody Credenciales body, HttpServletRequest request) {
        Usuario usuario = service.iniciarSesion(body.correo(), body.contrasena());
        // Sesion nueva en cada inicio de sesion para evitar la fijacion de sesion.
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        request.getSession(true).setAttribute(SesionUsuario.USUARIO_ID, usuario.id());
        return usuario;
    }

    @GetMapping("/sesion")
    public Usuario sesion(@RequestAttribute(SesionUsuario.USUARIO_ID) long usuarioId, HttpServletRequest request) {
        return service.usuarioActivo(usuarioId).orElseThrow(() -> {
            request.getSession().invalidate();
            return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesion para continuar.");
        });
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public record Credenciales(String correo, String contrasena) {
    }
}
