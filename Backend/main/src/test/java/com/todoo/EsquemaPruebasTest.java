package com.todoo;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Garantiza que las pruebas usen exactamente las tablas de database/propuesta_mysql.sql. */
class EsquemaPruebasTest {
    private static String desdeUsuarios(String sql) {
        String normalizado = sql.replace("\r\n", "\n");
        return normalizado.substring(normalizado.indexOf("CREATE TABLE usuarios")).strip();
    }

    @Test
    void laCopiaDePruebasCoincideConLaPropuesta() throws Exception {
        String propuesta = Files.readString(Path.of("database/propuesta_mysql.sql"));
        String copia = Files.readString(Path.of("src/test/resources/todoo_schema.sql"));
        assertEquals(desdeUsuarios(propuesta), desdeUsuarios(copia),
                "Actualiza src/test/resources/todoo_schema.sql con el contenido de database/propuesta_mysql.sql");
    }
}
