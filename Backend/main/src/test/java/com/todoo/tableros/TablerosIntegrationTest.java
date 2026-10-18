package com.todoo.tableros;

import com.todoo.DatosPrueba;
import com.todoo.seguridad.ContrasenaHasher;
import com.todoo.seguridad.SesionUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class TablerosIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired SesionUsuario sesion;
    @Autowired ContrasenaHasher hasher;
    private MockMvc mvc;
    private DatosPrueba datos;
    private long luis;
    private long diego;
    private long andrea;
    private long proyecto;
    private long diseno;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        datos = new DatosPrueba(jdbc, sesion, hasher);
        datos.limpiar();
        luis = datos.usuario("Luis", "luis@todoo.dev");
        diego = datos.usuario("Diego", "diego@todoo.dev");
        andrea = datos.usuario("Andrea", "andrea@todoo.dev");
        proyecto = datos.tablero("Proyecto final", luis);
        datos.miembro(proyecto, diego, "RESPONSABLE");
        datos.miembro(proyecto, andrea, "RESPONSABLE");
        diseno = datos.tablero("Diseno", diego);
    }

    @Test
    void exigeSesion() throws Exception {
        mvc.perform(get("/api/tableros")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/tableros/{id}", proyecto)).andExpect(status().isUnauthorized());
    }

    @Test
    void listaSoloLosTablerosDondeEsMiembroConSuRol() throws Exception {
        mvc.perform(get("/api/tableros").session(datos.login(diego)))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Diseno"))
                .andExpect(jsonPath("$[0].mi_rol").value("PROPIETARIO"))
                .andExpect(jsonPath("$[1].nombre").value("Proyecto final"))
                .andExpect(jsonPath("$[1].mi_rol").value("RESPONSABLE"))
                .andExpect(jsonPath("$[1].total_miembros").value(3));
        mvc.perform(get("/api/tableros").session(datos.login(andrea)))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(proyecto));
    }

    @Test
    void muestraElDetalleConLosResponsables() throws Exception {
        jdbc.update("UPDATE usuarios SET activo = FALSE WHERE id = ?", andrea);
        mvc.perform(get("/api/tableros/{id}", proyecto).session(datos.login(diego)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Proyecto final"))
                .andExpect(jsonPath("$.mi_rol").value("RESPONSABLE"))
                .andExpect(jsonPath("$.miembros.length()").value(2))
                .andExpect(jsonPath("$.miembros[0].nombre").value("Luis"))
                .andExpect(jsonPath("$.miembros[0].rol").value("PROPIETARIO"))
                .andExpect(jsonPath("$.miembros[1].nombre").value("Diego"));
    }

    @Test
    void ocultaLosTablerosAjenosOInexistentes() throws Exception {
        mvc.perform(get("/api/tableros/{id}", diseno).session(datos.login(andrea)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Tablero no encontrado."));
        mvc.perform(get("/api/tableros/{id}", 999999).session(datos.login(andrea)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/tableros/abc").session(datos.login(andrea)))
                .andExpect(status().isBadRequest());
    }
}
