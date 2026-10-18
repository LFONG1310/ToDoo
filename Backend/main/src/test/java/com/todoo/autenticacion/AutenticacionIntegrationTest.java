package com.todoo.autenticacion;

import com.todoo.DatosPrueba;
import com.todoo.seguridad.ContrasenaHasher;
import com.todoo.seguridad.SesionUsuario;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class AutenticacionIntegrationTest {
    @Autowired WebApplicationContext context;
    @Autowired JdbcTemplate jdbc;
    @Autowired SesionUsuario sesion;
    @Autowired ContrasenaHasher hasher;
    private MockMvc mvc;
    private DatosPrueba datos;
    private long luis;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        datos = new DatosPrueba(jdbc, sesion, hasher);
        datos.limpiar();
        luis = datos.usuario("Luis", "luis@todoo.dev");
    }

    private static String credenciales(String correo, String contrasena) {
        return "{\"correo\":\"" + correo + "\",\"contrasena\":\"" + contrasena + "\"}";
    }

    @Test
    void iniciaSesionConCredencialesCorrectasSinExponerElHash() throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("  LUIS@todoo.dev ", DatosPrueba.CONTRASENA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(luis))
                .andExpect(jsonPath("$.nombre").value("Luis"))
                .andExpect(jsonPath("$.contrasena_hash").doesNotExist())
                .andExpect(jsonPath("$.contrasenaHash").doesNotExist())
                .andReturn();
        HttpSession session = result.getRequest().getSession(false);
        assertNotNull(session);
        assertEquals(luis, session.getAttribute(SesionUsuario.USUARIO_ID));
    }

    @Test
    void rechazaContrasenaIncorrectaYCorreoInexistenteConElMismoMensaje() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("luis@todoo.dev", "otra")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contrasena incorrectos."));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("nadie@todoo.dev", DatosPrueba.CONTRASENA)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contrasena incorrectos."));
    }

    @Test
    void rechazaUsuariosDesactivados() throws Exception {
        jdbc.update("UPDATE usuarios SET activo = FALSE WHERE id = ?", luis);
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("luis@todoo.dev", DatosPrueba.CONTRASENA)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rechazaSolicitudesMalFormadas() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("no es json"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void creaUnaSesionNuevaAlIniciarSesion() throws Exception {
        MockHttpSession anterior = new MockHttpSession();
        mvc.perform(post("/api/auth/login").session(anterior).contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("luis@todoo.dev", DatosPrueba.CONTRASENA)))
                .andExpect(status().isOk());
        assertTrue(anterior.isInvalid());
    }

    @Test
    void consultaLaSesionActual() throws Exception {
        mvc.perform(get("/api/auth/sesion")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/sesion").session(datos.login(luis)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("luis@todoo.dev"));
    }

    @Test
    void cierraLaSesionConCsrf() throws Exception {
        MockHttpSession session = datos.login(luis);
        mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isForbidden());
        assertFalse(session.isInvalid());
        mvc.perform(post("/api/auth/logout").session(session).header(SesionUsuario.CSRF_HEADER, datos.csrf(session)))
                .andExpect(status().isNoContent());
        assertTrue(session.isInvalid());
    }

    @Test
    void entregaElTokenCsrfDeLaSesion() throws Exception {
        mvc.perform(get("/api/csrf")).andExpect(status().isUnauthorized());
        MockHttpSession session = datos.login(luis);
        mvc.perform(get("/api/csrf").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(datos.csrf(session)));
    }
}
