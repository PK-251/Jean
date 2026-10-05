package com.cineamazonas;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class FlujosTest {

    @Autowired
    private MockMvc web;

    @Autowired
    private JdbcTemplate bd;

    private MockHttpSession sesion() throws Exception {
        MvcResult resultado = web.perform(get("/login")).andReturn();
        return (MockHttpSession) resultado.getRequest().getSession();
    }

    private MvcResult ingresar(MockHttpSession sesion, String correo, String clave) throws Exception {
        return web.perform(post("/login").session(sesion)
                .param("csrf", (String) sesion.getAttribute("csrf"))
                .param("email", correo).param("password", clave)).andReturn();
    }

    private MvcResult ingresarAdmin(MockHttpSession sesion, String correo, String clave) throws Exception {
        return web.perform(post("/admin/login").session(sesion)
                .param("csrf", (String) sesion.getAttribute("csrf"))
                .param("email", correo).param("password", clave)).andReturn();
    }

    private MvcResult registrar(MockHttpSession sesion, String nombre, String correo, String clave,
                                String confirmacion) throws Exception {
        return web.perform(post("/registro").session(sesion)
                .param("csrf", (String) sesion.getAttribute("csrf"))
                .param("nombre", nombre).param("email", correo).param("password", clave)
                .param("confirmPassword", confirmacion).param("rol", "ADMIN")).andReturn();
    }

    @Test
    void carteleraLeePeliculasYFuncionesDesdeH2() throws Exception {
        MvcResult resultado = web.perform(get("/cartelera")).andReturn();
        assertEquals(200, resultado.getResponse().getStatus());
        assertEquals("cartelera", resultado.getModelAndView().getViewName());
        assertEquals(3, ((List<?>) resultado.getModelAndView().getModel().get("peliculas")).size());
        assertEquals(6, ((List<?>) resultado.getModelAndView().getModel().get("funciones")).size());
        assertEquals("/cartelera", web.perform(get("/")).andReturn().getResponse().getRedirectedUrl());
    }

    @Test
    void pestañasSeSeleccionanEnElServidor() throws Exception {
        MvcResult registro = web.perform(get("/login").param("panel", "register")).andReturn();
        assertEquals("register", registro.getModelAndView().getModel().get("panel"));
        MvcResult desconocido = web.perform(get("/login").param("panel", "otro")
                .param("destino", "https://ejemplo.com")).andReturn();
        assertEquals("login", desconocido.getModelAndView().getModel().get("panel"));
        assertEquals("cartelera", desconocido.getModelAndView().getModel().get("destino"));
    }

    @Test
    void registroValidaCamposSinGuardarDatosInvalidos() throws Exception {
        MockHttpSession sesion = sesion();
        int antes = bd.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class);
        MvcResult resultado = registrar(sesion, "", "incorrecto", "123", "456");
        assertEquals("login", resultado.getModelAndView().getViewName());
        assertEquals("register", resultado.getModelAndView().getModel().get("panel"));
        Map<?, ?> errores = (Map<?, ?>) resultado.getModelAndView().getModel().get("errores");
        assertEquals(4, errores.size());
        assertEquals(antes, bd.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class));
    }

    @Test
    void registroGuardaUsuarioNormalYContraseñaProtegida() throws Exception {
        MockHttpSession sesion = sesion();
        MvcResult resultado = registrar(sesion, "Persona de prueba", "NUEVO@TEST.PE", "Prueba123!", "Prueba123!");
        assertEquals("/login?registro=ok", resultado.getResponse().getRedirectedUrl());
        Map<String, Object> usuario = bd.queryForMap("SELECT * FROM usuarios WHERE correo = ?", "nuevo@test.pe");
        assertEquals("USER", usuario.get("rol"));
        assertNotEquals("Prueba123!", usuario.get("clave"));
        assertTrue(((Number) usuario.get("id")).longValue() >= 3);
        assertEquals("/cartelera", ingresar(sesion, "nuevo@test.pe", "Prueba123!")
                .getResponse().getRedirectedUrl());
    }

    @Test
    void correoDuplicadoNoSobrescribeElUsuario() throws Exception {
        MockHttpSession sesion = sesion();
        MvcResult resultado = registrar(sesion, "Otro", "ADMIN@CINEAMAZONAS.PE", "Prueba123!", "Prueba123!");
        assertEquals("login", resultado.getModelAndView().getViewName());
        assertEquals("Ese correo ya está registrado.", resultado.getModelAndView().getModel().get("mensaje"));
        assertEquals("Administrador Cine", bd.queryForObject(
                "SELECT nombre FROM usuarios WHERE correo = 'admin@cineamazonas.pe'", String.class));
    }

    @Test
    void formulariosRechazanSolicitudesSinToken() throws Exception {
        MockHttpSession sesion = sesion();
        MvcResult ingreso = web.perform(post("/login").session(sesion)
                .param("email", "admin@cineamazonas.pe").param("password", "Admin123!")).andReturn();
        assertEquals(403, ingreso.getResponse().getStatus());
        assertNull(sesion.getAttribute("usuarioId"));
        MvcResult registro = web.perform(post("/registro").session(sesion)
                .param("nombre", "Persona").param("email", "sin-token@test.pe")
                .param("password", "Prueba123!").param("confirmPassword", "Prueba123!")).andReturn();
        assertEquals(403, registro.getResponse().getStatus());
        assertEquals(0, bd.queryForObject("SELECT COUNT(*) FROM usuarios WHERE correo = 'sin-token@test.pe'", Integer.class));
    }

    @Test
    void loginValidaCamposYCredenciales() throws Exception {
        MockHttpSession sesion = sesion();
        MvcResult vacio = ingresar(sesion, "", "");
        assertEquals(2, ((Map<?, ?>) vacio.getModelAndView().getModel().get("errores")).size());
        MvcResult incorrecto = ingresar(sesion, "admin@cineamazonas.pe", "incorrecta");
        assertEquals("Correo o contraseña incorrectos.", incorrecto.getModelAndView().getModel().get("mensaje"));
        assertNull(sesion.getAttribute("usuarioId"));
        MvcResult inexistente = ingresar(sesion, "nadie@test.pe", "Prueba123!");
        assertEquals("Correo o contraseña incorrectos.", inexistente.getModelAndView().getModel().get("mensaje"));
    }

    @Test
    void loginAdminRenuevaSesionYPermiteAccesoAlPanel() throws Exception {
        MockHttpSession sesion = sesion();
        String idAnterior = sesion.getId();
        String tokenAnterior = (String) sesion.getAttribute("csrf");
        assertEquals("/admin", ingresarAdmin(sesion, "admin@cineamazonas.pe", "Admin123!")
                .getResponse().getRedirectedUrl());
        assertNotEquals(idAnterior, sesion.getId());
        assertNotEquals(tokenAnterior, sesion.getAttribute("csrf"));
        assertEquals("ADMIN", sesion.getAttribute("rol"));
        MvcResult panel = web.perform(get("/admin").session(sesion)).andReturn();
        assertEquals(200, panel.getResponse().getStatus());
        assertEquals("admin", panel.getModelAndView().getViewName());
        assertEquals(3, ((List<?>) panel.getModelAndView().getModel().get("salas")).size());
    }

    @Test
    void usuariosNoPuedenAccederAlPanelAdmin() throws Exception {
        assertEquals("/admin/login", web.perform(get("/admin")).andReturn()
                .getResponse().getRedirectedUrl());
        MockHttpSession sesion = sesion();
        ingresar(sesion, "cliente@cineamazonas.pe", "Cliente123!");
        assertEquals("/admin/login", web.perform(get("/admin").session(sesion)).andReturn().getResponse().getRedirectedUrl());
    }

    @Test
    void ticketsSoloMuestranLosDelUsuarioConSesion() throws Exception {
        assertEquals("/login?destino=tickets", web.perform(get("/mis-tickets")).andReturn()
                .getResponse().getRedirectedUrl());
        MockHttpSession cliente = sesion();
        ingresar(cliente, "cliente@cineamazonas.pe", "Cliente123!");
        MvcResult resultado = web.perform(get("/mis-tickets").session(cliente)).andReturn();
        List<?> tickets = (List<?>) resultado.getModelAndView().getModel().get("tickets");
        assertEquals(3, tickets.size());
        assertEquals("001", ((Map<?, ?>) tickets.get(0)).get("numero"));
        assertEquals("Vigente", ((Map<?, ?>) tickets.get(0)).get("estado"));
        MockHttpSession admin = sesion();
        ingresarAdmin(admin, "admin@cineamazonas.pe", "Admin123!");
        MvcResult sinTickets = web.perform(get("/mis-tickets").session(admin)).andReturn();
        assertEquals("/login?destino=tickets", sinTickets.getResponse().getRedirectedUrl());
    }

    @Test
    void mantenerSesionAjustaTiempoYCookies() throws Exception {
        MockHttpSession sesion = sesion();
        MvcResult resultado = web.perform(post("/login").session(sesion)
                .param("csrf", (String) sesion.getAttribute("csrf"))
                .param("email", "cliente@cineamazonas.pe").param("password", "Cliente123!")
                .param("remember", "on").param("destino", "tickets")).andReturn();
        assertEquals("/mis-tickets", resultado.getResponse().getRedirectedUrl());
        assertEquals(604800, sesion.getMaxInactiveInterval());
        assertEquals(604800, resultado.getResponse().getCookie("JSESSIONID").getMaxAge());
        assertTrue(resultado.getResponse().getCookie("JSESSIONID").isHttpOnly());
    }

    @Test
    void losAccesosRechazanCuentasDelOtroRol() throws Exception {
        MockHttpSession cliente = sesion();
        MvcResult accesoAdmin = ingresarAdmin(cliente, "cliente@cineamazonas.pe", "Cliente123!");
        assertEquals(403, accesoAdmin.getResponse().getStatus());
        assertEquals("admin-login", accesoAdmin.getModelAndView().getViewName());
        assertNull(cliente.getAttribute("usuarioId"));
        MockHttpSession admin = sesion();
        MvcResult accesoCliente = ingresar(admin, "admin@cineamazonas.pe", "Admin123!");
        assertEquals(403, accesoCliente.getResponse().getStatus());
        assertEquals("login", accesoCliente.getModelAndView().getViewName());
        assertNull(admin.getAttribute("usuarioId"));
        assertEquals("admin-login", web.perform(get("/admin/login")).andReturn().getModelAndView().getViewName());
    }

    @Test
    void cerrarSesionRequiereTokenYEliminaElAcceso() throws Exception {
        MockHttpSession sesion = sesion();
        ingresar(sesion, "cliente@cineamazonas.pe", "Cliente123!");
        MvcResult invalido = web.perform(post("/salir").session(sesion)).andReturn();
        assertEquals(403, invalido.getResponse().getStatus());
        assertNotNull(sesion.getAttribute("usuarioId"));
        MvcResult salida = web.perform(post("/salir").session(sesion)
                .param("csrf", (String) sesion.getAttribute("csrf"))).andReturn();
        assertEquals("/login", salida.getResponse().getRedirectedUrl());
        assertTrue(sesion.isInvalid());
    }

    @Test
    void continuarInvitadoNuncaAbreHistorialNiRedirigeFueraDelSitio() throws Exception {
        MockHttpSession sesion = sesion();
        assertEquals("/cartelera", web.perform(post("/invitado").session(sesion).param("destino", "tickets")
                .param("csrf", (String) sesion.getAttribute("csrf")))
                .andReturn().getResponse().getRedirectedUrl());
        assertEquals("/cartelera", web.perform(post("/invitado").session(sesion).param("destino", "https://ejemplo.com")
                .param("csrf", (String) sesion.getAttribute("csrf")))
                .andReturn().getResponse().getRedirectedUrl());
        sesion.setAttribute("funcionPendiente", 2);
        assertEquals("/sala?funcion=2", web.perform(post("/invitado").session(sesion).param("destino", "sala")
                .param("csrf", (String) sesion.getAttribute("csrf")))
                .andReturn().getResponse().getRedirectedUrl());
        assertNull(sesion.getAttribute("usuarioId"));
        assertEquals("/admin/login", web.perform(get("/admin").session(sesion)).andReturn().getResponse().getRedirectedUrl());
        assertEquals(403, web.perform(post("/invitado").session(sesion).param("destino", "compra"))
                .andReturn().getResponse().getStatus());
    }
}
