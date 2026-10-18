package com.todoo.proyectos;

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
class ProyectosIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired SesionUsuario sesion;
    @Autowired ContrasenaHasher hasher;
    private MockMvc mvc;
    private DatosPrueba datos;
    private long luis;
    private long diego;
    private long andrea;
    private long principal;
    private long diseno;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        datos = new DatosPrueba(jdbc, sesion, hasher);
        datos.limpiar();
        luis = datos.usuario("Luis", "luis@todoo.dev");
        diego = datos.usuario("Diego", "diego@todoo.dev");
        andrea = datos.usuario("Andrea", "andrea@todoo.dev");
        principal = datos.proyecto("Proyecto final", luis);
        datos.miembro(principal, diego, "miembro");
        datos.miembro(principal, andrea, "miembro");
        diseno = datos.proyecto("Diseno", diego);
    }

    @Test
    void exigeSesion() throws Exception {
        mvc.perform(get("/api/proyectos")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/proyectos/{id}", principal)).andExpect(status().isUnauthorized());
    }

    @Test
    void listaSoloLosProyectosActivosDondeEsMiembroConSuRol() throws Exception {
        mvc.perform(get("/api/proyectos").session(datos.login(diego)))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Diseno"))
                .andExpect(jsonPath("$[0].mi_rol").value("administrador"))
                .andExpect(jsonPath("$[1].nombre").value("Proyecto final"))
                .andExpect(jsonPath("$[1].mi_rol").value("miembro"))
                .andExpect(jsonPath("$[1].total_miembros").value(3));
        jdbc.update("UPDATE proyectos SET archivado = TRUE WHERE id = ?", diseno);
        mvc.perform(get("/api/proyectos").session(datos.login(diego)))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void muestraElDetalleConLosMiembrosActivos() throws Exception {
        jdbc.update("UPDATE miembros_proyecto SET activo = FALSE WHERE usuario_id = ? AND proyecto_id = ?", andrea, principal);
        mvc.perform(get("/api/proyectos/{id}", principal).session(datos.login(diego)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Proyecto final"))
                .andExpect(jsonPath("$.mi_rol").value("miembro"))
                .andExpect(jsonPath("$.miembros.length()").value(2))
                .andExpect(jsonPath("$.miembros[0].nombre").value("Luis"))
                .andExpect(jsonPath("$.miembros[0].rol").value("administrador"))
                .andExpect(jsonPath("$.miembros[1].nombre").value("Diego"));
    }

    @Test
    void ocultaLosProyectosAjenosInexistentesOAbandonados() throws Exception {
        mvc.perform(get("/api/proyectos/{id}", diseno).session(datos.login(andrea)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Proyecto no encontrado."));
        mvc.perform(get("/api/proyectos/{id}", 999999).session(datos.login(andrea)))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/proyectos/abc").session(datos.login(andrea)))
                .andExpect(status().isBadRequest());
        jdbc.update("UPDATE miembros_proyecto SET activo = FALSE WHERE usuario_id = ? AND proyecto_id = ?", andrea, principal);
        mvc.perform(get("/api/proyectos/{id}", principal).session(datos.login(andrea)))
                .andExpect(status().isNotFound());
    }
}
