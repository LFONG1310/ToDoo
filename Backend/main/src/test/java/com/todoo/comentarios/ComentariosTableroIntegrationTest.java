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
class ComentariosTableroIntegrationTest {
    private static final String URL = "/api/tableros/{tableroId}/comentarios";
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
        diseno = datos.tablero("Diseno", andrea);
    }

    private static String json(String texto) {
        return "{\"texto\":\"" + texto + "\"}";
    }

    private ResultActions publicar(long tableroId, long usuarioId, String body) throws Exception {
        MockHttpSession session = datos.login(usuarioId);
        return mvc.perform(post(URL, tableroId).session(session).header(SesionUsuario.CSRF_HEADER, datos.csrf(session))
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private long contar() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM comentarios_tablero", Long.class);
    }

    @Test
    void exigeSesionParaLeerYEscribir() throws Exception {
        mvc.perform(get(URL, proyecto)).andExpect(status().isUnauthorized());
        mvc.perform(post(URL, proyecto).contentType(MediaType.APPLICATION_JSON).content(json("Hola")))
                .andExpect(status().isUnauthorized());
        assertEquals(0, contar());
    }

    @Test
    void losMiembrosVenLaConversacionEnOrdenConElNombreDelAutor() throws Exception {
        datos.comentario(proyecto, diego, "Segundo", LocalDateTime.of(2026, 10, 17, 15, 30));
        datos.comentario(proyecto, luis, "Primero", LocalDateTime.of(2026, 10, 17, 15, 0));
        datos.comentario(diseno, andrea, "De otro tablero", LocalDateTime.of(2026, 10, 17, 16, 0));
        mvc.perform(get(URL, proyecto).session(datos.login(diego)))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].texto").value("Primero"))
                .andExpect(jsonPath("$[0].autor_nombre").value("Luis"))
                .andExpect(jsonPath("$[0].usuario_id").value(luis))
                .andExpect(jsonPath("$[0].fecha_creacion").value("2026-10-17T15:00:00Z"))
                .andExpect(jsonPath("$[0].fecha_edicion").doesNotExist())
                .andExpect(jsonPath("$[1].texto").value("Segundo"));
    }

    @Test
    void devuelveSoloLosComentariosMasRecientes() throws Exception {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
        for (int i = 0; i < ComentarioService.LIMITE_LISTA + 5; i++) {
            datos.comentario(proyecto, luis, "Mensaje " + i, base.plusMinutes(i));
        }
        mvc.perform(get(URL, proyecto).session(datos.login(luis)))
                .andExpect(jsonPath("$.length()").value(ComentarioService.LIMITE_LISTA))
                .andExpect(jsonPath("$[0].texto").value("Mensaje 5"))
                .andExpect(jsonPath("$[" + (ComentarioService.LIMITE_LISTA - 1) + "].texto")
                        .value("Mensaje " + (ComentarioService.LIMITE_LISTA + 4)));
    }

    @Test
    void quienNoEsMiembroNoPuedeLeerNiEscribir() throws Exception {
        mvc.perform(get(URL, proyecto).session(datos.login(andrea)))
                .andExpect(status().isNotFound());
        publicar(proyecto, andrea, json("Me cuelo")).andExpect(status().isNotFound());
        mvc.perform(get(URL, 999999).session(datos.login(luis))).andExpect(status().isNotFound());
        assertEquals(0, contar());
    }

    @Test
    void exigeCsrfParaPublicar() throws Exception {
        mvc.perform(post(URL, proyecto).session(datos.login(diego))
                        .contentType(MediaType.APPLICATION_JSON).content(json("Hola")))
                .andExpect(status().isForbidden());
        assertEquals(0, contar());
    }

    @Test
    void publicaUnComentarioRecortadoYLoDevuelve() throws Exception {
        publicar(proyecto, diego, json("  Ya subi los cambios  "))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.texto").value("Ya subi los cambios"))
                .andExpect(jsonPath("$.autor_nombre").value("Diego"))
                .andExpect(jsonPath("$.tablero_id").value(proyecto))
                .andExpect(jsonPath("$.usuario_id").value(diego));
        assertEquals(1, contar());
        mvc.perform(get(URL, proyecto).session(datos.login(luis)))
                .andExpect(jsonPath("$[0].texto").value("Ya subi los cambios"));
    }

    @Test
    void ignoraElUsuarioQueMandeElNavegador() throws Exception {
        publicar(proyecto, diego, "{\"texto\":\"Hola\",\"usuario_id\":" + luis + "}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario_id").value(diego));
    }

    @Test
    void rechazaComentariosVaciosODemasiadoLargos() throws Exception {
        publicar(proyecto, diego, json("   ")).andExpect(status().isBadRequest());
        publicar(proyecto, diego, "{}").andExpect(status().isBadRequest());
        publicar(proyecto, diego, "texto").andExpect(status().isBadRequest());
        publicar(proyecto, diego, json("a".repeat(ComentarioService.MAX_TEXTO + 1))).andExpect(status().isBadRequest());
        assertEquals(0, contar());
        publicar(proyecto, diego, json("a".repeat(ComentarioService.MAX_TEXTO))).andExpect(status().isCreated());
    }

    @Test
    void elAutorEditaSuComentarioYQuedaMarcado() throws Exception {
        long id = datos.comentario(proyecto, diego, "Original", LocalDateTime.of(2026, 10, 17, 15, 0));
        MockHttpSession session = datos.login(diego);
        mvc.perform(patch(URL_UNO, proyecto, id).session(session).header(SesionUsuario.CSRF_HEADER, datos.csrf(session))
                        .contentType(MediaType.APPLICATION_JSON).content(json("Corregido")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.texto").value("Corregido"))
                .andExpect(jsonPath("$.fecha_edicion").value(notNullValue()));
        assertEquals("Corregido", jdbc.queryForObject("SELECT texto FROM comentarios_tablero WHERE id = ?", String.class, id));
    }

    @Test
    void nadieMasPuedeEditarElComentario() throws Exception {
        long id = datos.comentario(proyecto, diego, "Original", LocalDateTime.of(2026, 10, 17, 15, 0));
        MockHttpSession session = datos.login(luis);
        mvc.perform(patch(URL_UNO, proyecto, id).session(session).header(SesionUsuario.CSRF_HEADER, datos.csrf(session))
                        .contentType(MediaType.APPLICATION_JSON).content(json("Cambiado por el propietario")))
                .andExpect(status().isForbidden());
        assertEquals("Original", jdbc.queryForObject("SELECT texto FROM comentarios_tablero WHERE id = ?", String.class, id));
    }

    @Test
    void noSePuedeTocarUnComentarioDesdeOtroTablero() throws Exception {
        long ajeno = datos.comentario(diseno, andrea, "De diseno", LocalDateTime.of(2026, 10, 17, 15, 0));
        datos.miembro(diseno, luis, "RESPONSABLE");
        MockHttpSession session = datos.login(luis);
        // Luis es propietario de "proyecto", pero el comentario pertenece a "diseno".
        mvc.perform(delete(URL_UNO, proyecto, ajeno).session(session).header(SesionUsuario.CSRF_HEADER, datos.csrf(session)))
                .andExpect(status().isNotFound());
        assertEquals(1, contar());
    }

    @Test
    void elAutorYElPropietarioPuedenEliminarPeroOtroResponsableNo() throws Exception {
        long deLuis = datos.comentario(proyecto, luis, "Del propietario", LocalDateTime.of(2026, 10, 17, 15, 0));
        long deDiego = datos.comentario(proyecto, diego, "De Diego", LocalDateTime.of(2026, 10, 17, 15, 5));
        long otroDeDiego = datos.comentario(proyecto, diego, "Otro de Diego", LocalDateTime.of(2026, 10, 17, 15, 10));

        MockHttpSession diegoSession = datos.login(diego);
        String csrfDiego = datos.csrf(diegoSession);
        mvc.perform(delete(URL_UNO, proyecto, deLuis).session(diegoSession).header(SesionUsuario.CSRF_HEADER, csrfDiego))
                .andExpect(status().isForbidden());
        mvc.perform(delete(URL_UNO, proyecto, deDiego).session(diegoSession).header(SesionUsuario.CSRF_HEADER, csrfDiego))
                .andExpect(status().isNoContent());

        MockHttpSession luisSession = datos.login(luis);
        mvc.perform(delete(URL_UNO, proyecto, otroDeDiego).session(luisSession).header(SesionUsuario.CSRF_HEADER, datos.csrf(luisSession)))
                .andExpect(status().isNoContent());

        assertEquals(1, contar());
        mvc.perform(delete(URL_UNO, proyecto, deDiego).session(diegoSession).header(SesionUsuario.CSRF_HEADER, csrfDiego))
                .andExpect(status().isNotFound());
    }

    @Test
    void alBorrarElTableroSeBorraSuConversacion() {
        datos.comentario(proyecto, luis, "Hola", LocalDateTime.of(2026, 10, 17, 15, 0));
        jdbc.update("DELETE FROM tableros WHERE id = ?", proyecto);
        assertEquals(0, contar());
    }
}
