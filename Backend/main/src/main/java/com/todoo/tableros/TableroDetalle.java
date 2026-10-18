package com.todoo.tableros;

import java.util.List;

public record TableroDetalle(
        long id,
        String nombre,
        String descripcion,
        String mi_rol,
        List<Miembro> miembros) {
}
