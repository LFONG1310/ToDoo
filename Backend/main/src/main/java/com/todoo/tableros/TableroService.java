package com.todoo.tableros;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TableroService {
    public static final String PROPIETARIO = "PROPIETARIO";
    private final TableroRepository repository;

    public TableroService(TableroRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Tablero> listar(long usuarioId) {
        return repository.listarDeUsuario(usuarioId);
    }

    @Transactional(readOnly = true)
    public TableroDetalle detalle(long tableroId, long usuarioId) {
        Tablero tablero = repository.buscarParaMiembro(tableroId, usuarioId).orElseThrow(TableroService::noEncontrado);
        return new TableroDetalle(tablero.id(), tablero.nombre(), tablero.descripcion(), tablero.mi_rol(),
                repository.listarMiembros(tableroId));
    }

    /**
     * Devuelve el rol del usuario en el tablero. Si no es miembro responde 404 (y no 403)
     * para no revelar que el tablero existe.
     */
    @Transactional(readOnly = true)
    public String exigirMiembro(long tableroId, long usuarioId) {
        return repository.rolDe(tableroId, usuarioId).orElseThrow(TableroService::noEncontrado);
    }

    private static ResponseStatusException noEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Tablero no encontrado.");
    }
}
