package com.todoo.tareas;

import com.todoo.seguridad.SesionUsuario;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:tareas;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.sql.init.mode=always", "spring.sql.init.schema-locations=classpath:tareas_schema.sql"
})
class TareasFiltrosIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM tareas");
        jdbc.update("DELETE FROM miembros_proyecto");
        jdbc.update("DELETE FROM usuarios");
        jdbc.update("INSERT INTO usuarios (id, nombre) VALUES (1, 'Ana'), (2, 'Beto'), (3, 'Carla'), (4, 'Dani')");
        // Carla pertenece a otro proyecto; Dani dejo el proyecto 1.
        jdbc.update("INSERT INTO miembros_proyecto (proyecto_id, usuario_id, activo)"
                + " VALUES (1, 1, TRUE), (1, 2, TRUE), (2, 3, TRUE), (1, 4, FALSE)");
        tarea(1, "Diseñar login", 1, "alta", "todo");
        tarea(1, "Crear API", 2, "media", "in-progress");
        tarea(1, "Probar filtros", 1, "baja", "done");
        tarea(1, "Revisar estilos", 2, "alta", "todo");
        tarea(2, "Tarea ajena", 3, "alta", "todo");
        jdbc.update("UPDATE tareas SET archivada = TRUE WHERE titulo = 'Revisar estilos'");
        tarea(1, "Documentar", 2, "alta", "todo");
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private void tarea(long proyecto, String titulo, long responsable, String prioridad, String estado) {
        jdbc.update("INSERT INTO tareas (proyecto_id, titulo, descripcion, responsable_id, prioridad, estado, fecha_limite)"
                + " VALUES (?, ?, '', ?, ?, ?, ?)", proyecto, titulo, responsable, prioridad, estado,
                LocalDateTime.of(2026, 11, 1, 12, 0));
    }

    private MockHttpSession login(long id) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(SesionUsuario.USUARIO_ID, id);
        return session;
    }

    @Test
    void exigeSesion() throws Exception {
        mvc.perform(get("/api/proyectos/1/tareas")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/proyectos/1/responsables")).andExpect(status().isUnauthorized());
    }

    @Test
    void sinFiltrosDevuelveLasTareasActivasDelProyecto() throws Exception {
        mvc.perform(get("/api/proyectos/1/tareas").session(login(1)))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$[*].titulo", contains("Probar filtros", "Crear API", "Diseñar login", "Documentar")))
                .andExpect(jsonPath("$[0].responsable.nombre").value("Ana"));
    }

    @Test
    void filtraPorPrioridad() throws Exception {
        mvc.perform(get("/api/proyectos/1/tareas").param("prioridad", "alta").session(login(1)))
                .andExpect(jsonPath("$[*].titulo", contains("Diseñar login", "Documentar")));
    }

    @Test
    void filtraPorResponsableYCombinaConPrioridad() throws Exception {
        mvc.perform(get("/api/proyectos/1/tareas").param("responsableId", "2").session(login(1)))
                .andExpect(jsonPath("$[*].titulo", contains("Crear API", "Documentar")));
        mvc.perform(get("/api/proyectos/1/tareas").param("responsableId", "2").param("prioridad", "alta")
                        .session(login(1)))
                .andExpect(jsonPath("$[*].titulo", contains("Documentar")));
    }

    @Test
    void soloMiasUsaElUsuarioDeLaSesion() throws Exception {
        mvc.perform(get("/api/proyectos/1/tareas").param("soloMias", "true").session(login(2)))
                .andExpect(jsonPath("$[*].titulo", contains("Crear API", "Documentar")));
        // Pedir otro responsable junto con "solo mias" no puede devolver tareas ajenas.
        mvc.perform(get("/api/proyectos/1/tareas").param("soloMias", "true").param("responsableId", "1")
                        .session(login(2)))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rechazaFiltrosInvalidos() throws Exception {
        mvc.perform(get("/api/proyectos/1/tareas").param("prioridad", "urgente").session(login(1)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/proyectos/1/tareas").param("responsableId", "abc").session(login(1)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void noMuestraProyectosAjenos() throws Exception {
        mvc.perform(get("/api/proyectos/2/tareas").session(login(1))).andExpect(status().isNotFound());
        mvc.perform(get("/api/proyectos/2/responsables").session(login(1))).andExpect(status().isNotFound());
        mvc.perform(get("/api/proyectos/1/tareas").session(login(4))).andExpect(status().isNotFound());
    }

    @Test
    void listaLosResponsablesActivosDelProyecto() throws Exception {
        mvc.perform(get("/api/proyectos/1/responsables").session(login(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre", contains("Ana", "Beto")));
    }
}
