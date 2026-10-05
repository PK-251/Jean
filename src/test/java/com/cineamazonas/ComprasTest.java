package com.cineamazonas;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
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

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:compras;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class ComprasTest {

    @Autowired
    private MockMvc web;

    @Autowired
    private JdbcTemplate bd;

    @BeforeEach
    void prepararDatos() {
        bd.update("DELETE FROM tickets WHERE compra_id IS NOT NULL");
        bd.update("DELETE FROM compras");
        bd.update("UPDATE funciones SET activo = TRUE, fecha = CURRENT_DATE WHERE id <= 6");
        bd.update("UPDATE salas SET activo = TRUE");
        bd.update("UPDATE salas SET aforo = 80 WHERE id = 1");
        bd.update("UPDATE salas SET aforo = 65 WHERE id = 2");
        bd.update("UPDATE salas SET aforo = 90 WHERE id = 3");
    }

    private MockHttpSession cliente() throws Exception {
        MvcResult pagina = web.perform(get("/login")).andReturn();
        MockHttpSession sesion = (MockHttpSession) pagina.getRequest().getSession();
        web.perform(post("/login").session(sesion).param("csrf", (String) sesion.getAttribute("csrf"))
                .param("email", "cliente@cineamazonas.pe").param("password", "Cliente123!")).andReturn();
        return sesion;
    }

    private String abrirSala(MockHttpSession sesion, int funcion) throws Exception {
        MvcResult pagina = web.perform(get("/sala").session(sesion).param("funcion", String.valueOf(funcion))).andReturn();
        assertEquals(200, pagina.getResponse().getStatus());
        return (String) pagina.getModelAndView().getModel().get("compra");
    }

    private MvcResult seleccionar(MockHttpSession sesion, int funcion, String compra, int cantidad) throws Exception {
        return web.perform(post("/seleccionar").session(sesion)
                .param("funcion", String.valueOf(funcion)).param("compra", compra)
                .param("csrf", (String) sesion.getAttribute("csrf"))
                .param("cantidad", String.valueOf(cantidad))
                .param("precio", "0.01").param("total", "0.01")).andReturn();
    }

    private MvcResult confirmar(MockHttpSession sesion, int funcion, String compra) throws Exception {
        return web.perform(post("/comprar").session(sesion).param("funcion", String.valueOf(funcion))
                .param("compra", compra).param("csrf", (String) sesion.getAttribute("csrf"))
                .param("cantidad", "9").param("total", "0.01")).andReturn();
    }

    private MvcResult comprar(MockHttpSession sesion, int funcion, String compra, int cantidad) throws Exception {
        MvcResult seleccion = seleccionar(sesion, funcion, compra, cantidad);
        if (seleccion.getResponse().getStatus() >= 400) {
            return seleccion;
        }
        if (!"USER".equals(sesion.getAttribute("rol"))) {
            web.perform(post("/invitado").session(sesion).param("destino", "compra")
                    .param("csrf", (String) sesion.getAttribute("csrf"))).andReturn();
        }
        return confirmar(sesion, funcion, compra);
    }

    @Test
    void compraGuardaTicketsConTurnoYTotalDelServidor() throws Exception {
        MockHttpSession sesion = cliente();
        String compra = abrirSala(sesion, 1);
        MvcResult resultado = comprar(sesion, 1, compra, 2);
        assertEquals("/mis-tickets?compra=" + compra, resultado.getResponse().getRedirectedUrl());
        Map<String, Object> guardada = bd.queryForMap("SELECT * FROM compras WHERE id = ?", compra);
        assertEquals(2, guardada.get("cantidad"));
        assertEquals(new BigDecimal("30.00"), guardada.get("total"));
        List<Map<String, Object>> tickets = bd.queryForList("SELECT * FROM tickets WHERE compra_id = ? ORDER BY turno", compra);
        assertEquals(2, tickets.size());
        assertEquals(1, tickets.get(0).get("turno"));
        assertEquals(2, tickets.get(1).get("turno"));
        assertFalse(tickets.get(0).containsKey("asiento"));
        MvcResult historial = web.perform(get("/mis-tickets").session(sesion).param("compra", compra)).andReturn();
        assertNotNull(historial.getModelAndView().getModel().get("compraConfirmada"));
    }

    @Test
    void reenviarCompraNoDuplicaEntradas() throws Exception {
        MockHttpSession sesion = cliente();
        String compra = abrirSala(sesion, 1);
        comprar(sesion, 1, compra, 2);
        MvcResult repetida = confirmar(sesion, 1, compra);
        assertEquals("/mis-tickets?compra=" + compra, repetida.getResponse().getRedirectedUrl());
        assertEquals(1, bd.queryForObject("SELECT COUNT(*) FROM compras", Integer.class));
        assertEquals(2, bd.queryForObject("SELECT COUNT(*) FROM tickets WHERE compra_id = ?", Integer.class, compra));
    }

    @Test
    void lasComprasSiguientesContinuanLosTurnosEnOrdenDeLlegada() throws Exception {
        MockHttpSession primero = cliente();
        MockHttpSession segundo = cliente();
        String compra1 = abrirSala(primero, 1);
        String compra2 = abrirSala(segundo, 1);
        comprar(primero, 1, compra1, 2);
        comprar(segundo, 1, compra2, 3);
        List<Integer> turnos = bd.queryForList(
                "SELECT turno FROM tickets WHERE compra_id = ? ORDER BY turno", Integer.class, compra2);
        assertEquals(List.of(3, 4, 5), turnos);
    }

    @Test
    void cadaFuncionTieneSusPropiosTurnos() throws Exception {
        MockHttpSession sesion = cliente();
        comprar(sesion, 1, abrirSala(sesion, 1), 1);
        comprar(sesion, 2, abrirSala(sesion, 2), 1);
        assertEquals(2, bd.queryForObject("SELECT COUNT(*) FROM tickets WHERE turno = 1 AND compra_id IS NOT NULL", Integer.class));
    }

    @Test
    void noSeVendenMasEntradasQueElAforo() throws Exception {
        bd.update("UPDATE salas SET aforo = 3 WHERE id = 1");
        MockHttpSession primero = cliente();
        MockHttpSession segundo = cliente();
        String compra1 = abrirSala(primero, 1);
        String compra2 = abrirSala(segundo, 1);
        comprar(primero, 1, compra1, 2);
        MvcResult conflicto = comprar(segundo, 1, compra2, 2);
        assertEquals(409, conflicto.getResponse().getStatus());
        assertEquals(1, conflicto.getModelAndView().getModel().get("libres"));
        assertEquals(0, bd.queryForObject("SELECT COUNT(*) FROM compras WHERE id = ?", Integer.class, compra2));
    }

    @Test
    void cantidadInvalidaNoCreaCompras() throws Exception {
        MockHttpSession sesion = cliente();
        String compra = abrirSala(sesion, 1);
        assertEquals(400, comprar(sesion, 1, compra, 0).getResponse().getStatus());
        assertEquals(409, comprar(sesion, 1, compra, 11).getResponse().getStatus());
        assertEquals(0, bd.queryForObject("SELECT COUNT(*) FROM compras", Integer.class));
    }

    @Test
    void disponibilidadSeRevalidaAlGuardar() throws Exception {
        MockHttpSession sesion = cliente();
        String compra = abrirSala(sesion, 1);
        bd.update("UPDATE funciones SET activo = FALSE WHERE id = 1");
        MvcResult respuesta = comprar(sesion, 1, compra, 1);
        assertEquals(404, respuesta.getResponse().getStatus());
        assertEquals(0, bd.queryForObject("SELECT COUNT(*) FROM compras", Integer.class));
    }

    @Test
    void tokenDeFormularioYTokenDeCompraSonObligatorios() throws Exception {
        MockHttpSession sesion = cliente();
        String compra = abrirSala(sesion, 1);
        MvcResult sinToken = web.perform(post("/comprar").session(sesion)
                .param("funcion", "1").param("compra", compra).param("cantidad", "1")).andReturn();
        assertEquals(403, sinToken.getResponse().getStatus());
        assertEquals(403, comprar(sesion, 1, "invalido", 1).getResponse().getStatus());
        assertEquals(0, bd.queryForObject("SELECT COUNT(*) FROM compras", Integer.class));
    }

    @Test
    void loginDeClienteRegresaALaFuncionElegida() throws Exception {
        MvcResult sala = web.perform(get("/sala").param("funcion", "2")).andReturn();
        assertEquals(200, sala.getResponse().getStatus());
        assertEquals("sala", sala.getModelAndView().getViewName());
        MockHttpSession sesion = (MockHttpSession) sala.getRequest().getSession();
        web.perform(get("/login").session(sesion).param("destino", "sala")).andReturn();
        MvcResult login = web.perform(post("/login").session(sesion)
                .param("csrf", (String) sesion.getAttribute("csrf"))
                .param("email", "cliente@cineamazonas.pe").param("password", "Cliente123!")
                .param("destino", "sala")).andReturn();
        assertEquals("/sala?funcion=2", login.getResponse().getRedirectedUrl());
    }

    @Test
    void invitadoCompraSinCuentaYRecibeSoloSuComprobante() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        String compra = abrirSala(sesion, 1);
        int cuentas = bd.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class);
        MvcResult resultado = comprar(sesion, 1, compra, 2);
        assertEquals("/compra?codigo=" + compra, resultado.getResponse().getRedirectedUrl());
        assertNull(sesion.getAttribute("usuarioId"));
        assertNull(sesion.getAttribute("rol"));
        assertEquals(cuentas, bd.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class));
        Map<String, Object> guardada = bd.queryForMap("SELECT * FROM compras WHERE id = ?", compra);
        assertNull(guardada.get("usuario_id"));
        assertEquals(sesion.getAttribute("invitadoId"), guardada.get("invitado"));
        assertEquals(new BigDecimal("30.00"), guardada.get("total"));
        assertEquals(2, bd.queryForObject("SELECT COUNT(*) FROM tickets WHERE compra_id = ? AND usuario_id IS NULL", Integer.class, compra));
        MvcResult comprobante = web.perform(get("/compra").session(sesion).param("codigo", compra)).andReturn();
        assertEquals(200, comprobante.getResponse().getStatus());
        assertEquals(2, ((List<?>) comprobante.getModelAndView().getModel().get("tickets")).size());
        assertEquals("/login?destino=tickets", web.perform(get("/mis-tickets").session(sesion)).andReturn()
                .getResponse().getRedirectedUrl());
    }

    @Test
    void comprobanteInvitadoNoSeComparteNiSeAdjuntaAlHistorial() throws Exception {
        MockHttpSession primero = new MockHttpSession();
        String compra = abrirSala(primero, 1);
        comprar(primero, 1, compra, 1);
        MockHttpSession otro = new MockHttpSession();
        abrirSala(otro, 1);
        assertEquals(404, web.perform(get("/compra").session(otro).param("codigo", compra)).andReturn()
                .getResponse().getStatus());
        assertEquals(403, comprar(otro, 1, compra, 1).getResponse().getStatus());
        assertEquals(404, web.perform(get("/compra").param("codigo", compra)).andReturn().getResponse().getStatus());
        MvcResult historial = web.perform(get("/mis-tickets").session(cliente())).andReturn();
        assertEquals(3, ((List<?>) historial.getModelAndView().getModel().get("tickets")).size());
    }

    @Test
    void invitadoNoDuplicaUnaCompra() throws Exception {
        MockHttpSession invitado = new MockHttpSession();
        String compra = abrirSala(invitado, 1);
        comprar(invitado, 1, compra, 1);
        assertEquals("/compra?codigo=" + compra, confirmar(invitado, 1, compra).getResponse().getRedirectedUrl());
        assertEquals(1, bd.queryForObject("SELECT COUNT(*) FROM compras", Integer.class));
        assertEquals(1, bd.queryForObject("SELECT COUNT(*) FROM tickets WHERE compra_id = ?", Integer.class, compra));
    }

    @Test
    void invitadoTambienRequiereTokensYCantidadValida() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        String compra = abrirSala(sesion, 1);
        assertEquals(400, comprar(sesion, 1, compra, 0).getResponse().getStatus());
        assertEquals(409, comprar(sesion, 1, compra, 11).getResponse().getStatus());
        assertEquals(403, web.perform(post("/comprar").session(sesion).param("funcion", "1")
                .param("compra", compra).param("cantidad", "1")).andReturn().getResponse().getStatus());
        assertEquals(0, bd.queryForObject("SELECT COUNT(*) FROM compras", Integer.class));
    }

    @Test
    void funcionInexistenteOAntiguaNoSePuedeComprar() throws Exception {
        MockHttpSession sesion = cliente();
        assertEquals(404, web.perform(get("/sala").session(sesion).param("funcion", "999")).andReturn().getResponse().getStatus());
        bd.update("UPDATE funciones SET fecha = CURRENT_DATE - 1 WHERE id = 1");
        assertEquals(404, web.perform(get("/sala").session(sesion).param("funcion", "1")).andReturn().getResponse().getStatus());
    }

    @Test
    void seleccionarSoloPreparaYObligaAElegirAccesoAntesDeComprar() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        String compra = abrirSala(sesion, 1);
        MvcResult seleccion = seleccionar(sesion, 1, compra, 2);
        assertEquals("/login?destino=compra", seleccion.getResponse().getRedirectedUrl());
        assertEquals(2, sesion.getAttribute("cantidadPendiente"));
        assertEquals(0, bd.queryForObject("SELECT COUNT(*) FROM compras", Integer.class));
        assertEquals("/login?destino=compra", confirmar(sesion, 1, compra).getResponse().getRedirectedUrl());
        assertEquals("/login?destino=compra", web.perform(get("/confirmar-compra").session(sesion)).andReturn()
                .getResponse().getRedirectedUrl());
        assertEquals(0, bd.queryForObject("SELECT COUNT(*) FROM compras", Integer.class));
        MvcResult invitado = web.perform(post("/invitado").session(sesion).param("destino", "compra")
                .param("csrf", (String) sesion.getAttribute("csrf"))).andReturn();
        assertEquals("/confirmar-compra", invitado.getResponse().getRedirectedUrl());
        MvcResult resumen = web.perform(get("/confirmar-compra").session(sesion)).andReturn();
        assertEquals("confirmar-compra", resumen.getModelAndView().getViewName());
        assertEquals(new BigDecimal("30.00"), resumen.getModelAndView().getModel().get("total"));
        assertEquals(0, bd.queryForObject("SELECT COUNT(*) FROM compras", Integer.class));
        assertEquals("/compra?codigo=" + compra, confirmar(sesion, 1, compra).getResponse().getRedirectedUrl());
    }

    @Test
    void loginTrasSeleccionarConservaLaCantidadYGuardaEnCuenta() throws Exception {
        MockHttpSession sesion = new MockHttpSession();
        String compra = abrirSala(sesion, 1);
        seleccionar(sesion, 1, compra, 2);
        MvcResult error = web.perform(post("/login").session(sesion).param("destino", "compra")
                .param("csrf", (String) sesion.getAttribute("csrf"))
                .param("email", "cliente@cineamazonas.pe").param("password", "incorrecta")).andReturn();
        assertEquals("login", error.getModelAndView().getViewName());
        assertEquals(2, sesion.getAttribute("cantidadPendiente"));
        MvcResult login = web.perform(post("/login").session(sesion).param("destino", "compra")
                .param("csrf", (String) sesion.getAttribute("csrf"))
                .param("email", "cliente@cineamazonas.pe").param("password", "Cliente123!")).andReturn();
        assertEquals("/confirmar-compra", login.getResponse().getRedirectedUrl());
        assertEquals(compra, sesion.getAttribute("compraToken"));
        assertEquals(0, bd.queryForObject("SELECT COUNT(*) FROM compras", Integer.class));
        assertEquals("/mis-tickets?compra=" + compra, confirmar(sesion, 1, compra).getResponse().getRedirectedUrl());
        assertEquals(2L, bd.queryForObject("SELECT usuario_id FROM compras WHERE id = ?", Long.class, compra));
        assertEquals(2, bd.queryForObject("SELECT COUNT(*) FROM tickets WHERE compra_id = ?", Integer.class, compra));
    }

    @Test
    void disponibilidadSeRevalidaDespuesDelLoginYAntesDeConfirmar() throws Exception {
        bd.update("UPDATE salas SET aforo = 3 WHERE id = 1");
        MockHttpSession primero = cliente();
        MockHttpSession segundo = cliente();
        String compra1 = abrirSala(primero, 1);
        String compra2 = abrirSala(segundo, 1);
        seleccionar(primero, 1, compra1, 2);
        seleccionar(segundo, 1, compra2, 2);
        assertEquals(302, confirmar(segundo, 1, compra2).getResponse().getStatus());
        assertEquals(409, confirmar(primero, 1, compra1).getResponse().getStatus());
        assertEquals(0, bd.queryForObject("SELECT COUNT(*) FROM tickets WHERE compra_id = ?", Integer.class, compra1));
    }

    @Test
    void confirmarSinSeleccionOConFormularioDeOtraFuncionNoCompra() throws Exception {
        MockHttpSession sesion = cliente();
        String compra = abrirSala(sesion, 1);
        assertEquals(400, confirmar(sesion, 1, compra).getResponse().getStatus());
        seleccionar(sesion, 1, compra, 1);
        abrirSala(sesion, 2);
        assertEquals(403, confirmar(sesion, 1, compra).getResponse().getStatus());
        assertEquals("/cartelera", web.perform(get("/confirmar-compra").session(sesion)).andReturn()
                .getResponse().getRedirectedUrl());
        assertEquals(0, bd.queryForObject("SELECT COUNT(*) FROM compras", Integer.class));
    }
}
