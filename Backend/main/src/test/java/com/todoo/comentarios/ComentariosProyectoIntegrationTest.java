package com.todoo.comentarios;

import com.todoo.DatosPrueba;
import com.todoo.seguridad.ContrasenaHasher;
import com.todoo.seguridad.SesionUsuario;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class ComentariosProyectoIntegrationTest {
    private static final String URL = "/api/proyectos/{proyectoId}/comentarios";
    private static final String URL_UNO = URL + "/{comentarioId}";
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
        diseno = datos.proyecto("Diseno", andrea);
    }

    private static String json(String texto) {
        return "{\"contenido\":\"" + texto + "\"}";
    }

    private ResultActions publicar(long proyectoId, long usuarioId, String body) throws Exception {
        MockHttpSession session = datos.login(usuarioId);
        return mvc.perform(post(URL, proyectoId).session(session).header(SesionUsuario.CSRF_HEADER, datos.csrf(session))
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private long contar() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM comentarios", Long.class);
    }

    @Test
    void exigeSesionParaLeerYEscribir() throws Exception {
        mvc.perform(get(URL, principal)).andExpect(status().isUnauthorized());
        mvc.perform(post(URL, principal).contentType(MediaType.APPLICATION_JSON).content(json("Hola")))
                .andExpect(status().isUnauthorized());
        assertEquals(0, contar());
    }

    @Test
    void losMiembrosVenLaConversacionEnOrdenConElNombreDelAutor() throws Exception {
        datos.comentario(principal, diego, "Segundo", LocalDateTime.of(2026, 10, 17, 15, 30));
        datos.comentario(principal, luis, "Primero", LocalDateTime.of(2026, 10, 17, 15, 0));
        datos.comentario(diseno, andrea, "De otro proyecto", LocalDateTime.of(2026, 10, 17, 16, 0));
        mvc.perform(get(URL, principal).session(datos.login(diego)))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].contenido").value("Primero"))
                .andExpect(jsonPath("$[0].autor_nombre").value("Luis"))
                .andExpect(jsonPath("$[0].autor_id").value(luis))
                .andExpect(jsonPath("$[0].fecha_creacion").value("2026-10-17T15:00:00Z"))
                .andExpect(jsonPath("$[0].fecha_edicion").doesNotExist())
                .andExpect(jsonPath("$[1].contenido").value("Segundo"));
    }

    @Test
    void devuelveSoloLosComentariosMasRecientes() throws Exception {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
        for (int i = 0; i < ComentarioService.LIMITE_LISTA + 5; i++) {
            datos.comentario(principal, luis, "Mensaje " + i, base.plusMinutes(i));
        }
        mvc.perform(get(URL, principal).session(datos.login(luis)))
                .andExpect(jsonPath("$.length()").value(ComentarioService.LIMITE_LISTA))
                .andExpect(jsonPath("$[0].contenido").value("Mensaje 5"))
                .andExpect(jsonPath("$[" + (ComentarioService.LIMITE_LISTA - 1) + "].contenido")
                        .value("Mensaje " + (ComentarioService.LIMITE_LISTA + 4)));
    }

    @Test
    void quienNoEsMiembroNoPuedeLeerNiEscribir() throws Exception {
        mvc.perform(get(URL, principal).session(datos.login(andrea)))
                .andExpect(status().isNotFound());
        publicar(principal, andrea, json("Me cuelo")).andExpect(status().isNotFound());
        mvc.perform(get(URL, 999999).session(datos.login(luis))).andExpect(status().isNotFound());
        assertEquals(0, contar());
    }

    @Test
    void exigeCsrfParaPublicar() throws Exception {
        mvc.perform(post(URL, principal).session(datos.login(diego))
                        .contentType(MediaType.APPLICATION_JSON).content(json("Hola")))
                .andExpect(status().isForbidden());
        assertEquals(0, contar());
    }

    @Test
    void publicaUnComentarioRecortadoYLoDevuelve() throws Exception {
        publicar(principal, diego, json("  Ya subi los cambios  "))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.contenido").value("Ya subi los cambios"))
                .andExpect(jsonPath("$.autor_nombre").value("Diego"))
                .andExpect(jsonPath("$.proyecto_id").value(principal))
                .andExpect(jsonPath("$.autor_id").value(diego));
        assertEquals(1, contar());
        mvc.perform(get(URL, principal).session(datos.login(luis)))
                .andExpect(jsonPath("$[0].contenido").value("Ya subi los cambios"));
    }

    @Test
    void ignoraElUsuarioQueMandeElNavegador() throws Exception {
        publicar(principal, diego, "{\"contenido\":\"Hola\",\"autor_id\":" + luis + "}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autor_id").value(diego));
    }

    @Test
    void rechazaComentariosVaciosODemasiadoLargos() throws Exception {
        publicar(principal, diego, json("   ")).andExpect(status().isBadRequest());
        publicar(principal, diego, "{}").andExpect(status().isBadRequest());
        publicar(principal, diego, "texto").andExpect(status().isBadRequest());
        publicar(principal, diego, json("a".repeat(ComentarioService.MAX_CONTENIDO + 1))).andExpect(status().isBadRequest());
        assertEquals(0, contar());
        publicar(principal, diego, json("a".repeat(ComentarioService.MAX_CONTENIDO))).andExpect(status().isCreated());
    }

    @Test
    void elAutorEditaSuComentarioYQuedaMarcado() throws Exception {
        long id = datos.comentario(principal, diego, "Original", LocalDateTime.of(2026, 10, 17, 15, 0));
        MockHttpSession session = datos.login(diego);
        mvc.perform(patch(URL_UNO, principal, id).session(session).header(SesionUsuario.CSRF_HEADER, datos.csrf(session))
                        .contentType(MediaType.APPLICATION_JSON).content(json("Corregido")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").value("Corregido"))
                .andExpect(jsonPath("$.fecha_edicion").value(notNullValue()));
        assertEquals("Corregido", jdbc.queryForObject("SELECT contenido FROM comentarios WHERE id = ?", String.class, id));
    }

    @Test
    void nadieMasPuedeEditarElComentario() throws Exception {
        long id = datos.comentario(principal, diego, "Original", LocalDateTime.of(2026, 10, 17, 15, 0));
        MockHttpSession session = datos.login(luis);
        mvc.perform(patch(URL_UNO, principal, id).session(session).header(SesionUsuario.CSRF_HEADER, datos.csrf(session))
                        .contentType(MediaType.APPLICATION_JSON).content(json("Cambiado por el propietario")))
                .andExpect(status().isForbidden());
        assertEquals("Original", jdbc.queryForObject("SELECT contenido FROM comentarios WHERE id = ?", String.class, id));
    }

    @Test
    void noSePuedeTocarUnComentarioDesdeOtroProyecto() throws Exception {
        long ajeno = datos.comentario(diseno, andrea, "De diseno", LocalDateTime.of(2026, 10, 17, 15, 0));
        datos.miembro(diseno, luis, "miembro");
        MockHttpSession session = datos.login(luis);
        // Luis es administrador de "principal", pero el comentario pertenece a "diseno".
        mvc.perform(delete(URL_UNO, principal, ajeno).session(session).header(SesionUsuario.CSRF_HEADER, datos.csrf(session)))
                .andExpect(status().isNotFound());
        assertEquals(1, contar());
    }

    @Test
    void elAutorYElAdministradorPuedenEliminarPeroOtroMiembroNo() throws Exception {
        long deLuis = datos.comentario(principal, luis, "Del administrador", LocalDateTime.of(2026, 10, 17, 15, 0));
        long deDiego = datos.comentario(principal, diego, "De Diego", LocalDateTime.of(2026, 10, 17, 15, 5));
        long otroDeDiego = datos.comentario(principal, diego, "Otro de Diego", LocalDateTime.of(2026, 10, 17, 15, 10));

        MockHttpSession diegoSession = datos.login(diego);
        String csrfDiego = datos.csrf(diegoSession);
        mvc.perform(delete(URL_UNO, principal, deLuis).session(diegoSession).header(SesionUsuario.CSRF_HEADER, csrfDiego))
                .andExpect(status().isForbidden());
        mvc.perform(delete(URL_UNO, principal, deDiego).session(diegoSession).header(SesionUsuario.CSRF_HEADER, csrfDiego))
                .andExpect(status().isNoContent());

        MockHttpSession luisSession = datos.login(luis);
        mvc.perform(delete(URL_UNO, principal, otroDeDiego).session(luisSession).header(SesionUsuario.CSRF_HEADER, datos.csrf(luisSession)))
                .andExpect(status().isNoContent());

        assertEquals(1, contar());
        mvc.perform(delete(URL_UNO, principal, deDiego).session(diegoSession).header(SesionUsuario.CSRF_HEADER, csrfDiego))
                .andExpect(status().isNotFound());
    }

    @Test
    void laConversacionDelProyectoNoMezclaLosComentariosDeTareas() throws Exception {
        long tarea = datos.tarea(principal, diego, "Disenar login");
        datos.comentario(principal, luis, "Del proyecto", LocalDateTime.of(2026, 10, 17, 15, 0));
        long deTarea = datos.comentarioDeTarea(principal, tarea, diego, "De la tarea", LocalDateTime.of(2026, 10, 17, 15, 5));
        mvc.perform(get(URL, principal).session(datos.login(luis)))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].contenido").value("Del proyecto"));
        MockHttpSession session = datos.login(diego);
        mvc.perform(delete(URL_UNO, principal, deTarea).session(session).header(SesionUsuario.CSRF_HEADER, datos.csrf(session)))
                .andExpect(status().isNotFound());
        assertEquals(2, contar());
    }

    @Test
    void unMiembroQueDejoElProyectoYaNoPuedeEscribir() throws Exception {
        jdbc.update("UPDATE miembros_proyecto SET activo = FALSE WHERE proyecto_id = ? AND usuario_id = ?", principal, diego);
        publicar(principal, diego, json("Sigo aqui?")).andExpect(status().isNotFound());
        assertEquals(0, contar());
    }
}
