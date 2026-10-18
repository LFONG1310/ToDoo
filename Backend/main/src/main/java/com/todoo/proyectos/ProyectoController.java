package com.todoo.proyectos;

import com.todoo.seguridad.SesionUsuario;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proyectos")
public class ProyectoController {
    private final ProyectoService service;

    public ProyectoController(ProyectoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Proyecto> listar(@RequestAttribute(SesionUsuario.USUARIO_ID) long usuarioId) {
        return service.listar(usuarioId);
    }

    @GetMapping("/{proyectoId}")
    public ProyectoDetalle detalle(@PathVariable long proyectoId, @RequestAttribute(SesionUsuario.USUARIO_ID) long usuarioId) {
        return service.detalle(proyectoId, usuarioId);
    }
}
