package com.todoo.tableros;

import com.todoo.seguridad.SesionUsuario;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tableros")
public class TableroController {
    private final TableroService service;

    public TableroController(TableroService service) {
        this.service = service;
    }

    @GetMapping
    public List<Tablero> listar(@RequestAttribute(SesionUsuario.USUARIO_ID) long usuarioId) {
        return service.listar(usuarioId);
    }

    @GetMapping("/{tableroId}")
    public TableroDetalle detalle(@PathVariable long tableroId, @RequestAttribute(SesionUsuario.USUARIO_ID) long usuarioId) {
        return service.detalle(tableroId, usuarioId);
    }
}
