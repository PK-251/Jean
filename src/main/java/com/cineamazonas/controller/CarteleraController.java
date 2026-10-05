package com.cineamazonas.controller;

import com.cineamazonas.service.CarteleraService;
import com.cineamazonas.service.CompraService;
import com.cineamazonas.service.TicketService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CarteleraController {

    private final CarteleraService carteleraService;
    private final CompraService compraService;
    private final TicketService ticketService;

    public CarteleraController(CarteleraService carteleraService, CompraService compraService,
                               TicketService ticketService) {
        this.carteleraService = carteleraService;
        this.compraService = compraService;
        this.ticketService = ticketService;
    }

    @GetMapping("/cartelera")
    public String mostrarCartelera(Model modelo) {
        modelo.addAttribute("peliculas", carteleraService.peliculasActivas());
        modelo.addAttribute("funciones", carteleraService.funcionesDeHoy());
        return "cartelera";
    }

    // Pantalla para elegir cuántas entradas comprar. No se elige asiento.
    @GetMapping("/sala")
    public String sala(@RequestParam int funcion, HttpSession sesion, HttpServletResponse respuesta, Model modelo) {
        sesion.setAttribute("funcionPendiente", funcion);
        if (sesion.getAttribute("csrf") == null) {
            sesion.setAttribute("csrf", UUID.randomUUID().toString());
        }
        if (sesion.getAttribute("invitadoId") == null) {
            sesion.setAttribute("invitadoId", UUID.randomUUID().toString());
        }
        String compra = UUID.randomUUID().toString();
        sesion.setAttribute("compraToken", compra);
        sesion.setAttribute("funcionCompra", funcion);
        sesion.removeAttribute("cantidadPendiente");
        sesion.removeAttribute("compraInvitado");
        mostrarSala(funcion, compra, 1, sesion, respuesta, modelo);
        return "sala";
    }

    @PostMapping("/seleccionar")
    public String seleccionar(@RequestParam int funcion,
                              @RequestParam(defaultValue = "") String compra,
                              @RequestParam(defaultValue = "") String csrf,
                              @RequestParam(defaultValue = "0") int cantidad,
                              HttpSession sesion, HttpServletResponse respuesta, Model modelo) {
        if (!formularioValido(funcion, compra, csrf, sesion)) {
            respuesta.setStatus(403);
            modelo.addAttribute("mensaje", "El formulario venció. Vuelve a abrir la función.");
            mostrarSala(funcion, compra, 1, sesion, respuesta, modelo);
            return "sala";
        }
        if (cantidad < 1) {
            respuesta.setStatus(400);
            modelo.addAttribute("mensaje", "Selecciona al menos una entrada para continuar.");
            mostrarSala(funcion, compra, 1, sesion, respuesta, modelo);
            return "sala";
        }
        Map<String, Object> sala = mostrarSala(funcion, compra, cantidad, sesion, respuesta, modelo);
        if (sala == null) {
            return "sala";
        }
        try {
            compraService.validarCantidad(cantidad, (Integer) sala.get("libres"));
        } catch (IllegalArgumentException error) {
            respuesta.setStatus(409);
            modelo.addAttribute("mensaje", error.getMessage());
            return "sala";
        }
        sesion.setAttribute("cantidadPendiente", cantidad);
        sesion.removeAttribute("compraInvitado");
        if ("USER".equals(sesion.getAttribute("rol"))) {
            return "redirect:/confirmar-compra";
        }
        return "redirect:/login?destino=compra";
    }

    @GetMapping("/confirmar-compra")
    @SuppressWarnings("unchecked")
    public String confirmar(HttpSession sesion, HttpServletResponse respuesta, Model modelo) {
        Integer cantidad = (Integer) sesion.getAttribute("cantidadPendiente");
        if (cantidad == null || cantidad < 1) {
            return "redirect:/cartelera";
        }
        boolean invitado = Boolean.TRUE.equals(sesion.getAttribute("compraInvitado"));
        if (!invitado && !"USER".equals(sesion.getAttribute("rol"))) {
            return "redirect:/login?destino=compra";
        }
        int funcion = (Integer) sesion.getAttribute("funcionCompra");
        String compra = (String) sesion.getAttribute("compraToken");
        Map<String, Object> sala = mostrarSala(funcion, compra, cantidad, sesion, respuesta, modelo);
        if (sala == null) {
            return "sala";
        }
        Map<String, Object> datos = (Map<String, Object>) sala.get("funcion");
        modelo.addAttribute("total", compraService.calcularTotal((BigDecimal) datos.get("precio"), cantidad));
        modelo.addAttribute("invitado", invitado);
        return "confirmar-compra";
    }

    @PostMapping("/comprar")
    public String comprar(@RequestParam int funcion,
                          @RequestParam(defaultValue = "") String compra,
                          @RequestParam(defaultValue = "") String csrf,
                          HttpSession sesion, HttpServletResponse respuesta, Model modelo) {
        if (csrf.isEmpty() || !csrf.equals(sesion.getAttribute("csrf"))) {
            respuesta.setStatus(403);
            modelo.addAttribute("mensaje", "El formulario venció. Vuelve a abrir la función.");
            mostrarSala(funcion, compra, 1, sesion, respuesta, modelo);
            return "sala";
        }
        boolean comoInvitado = Boolean.TRUE.equals(sesion.getAttribute("compraInvitado"));
        if (!comoInvitado && !"USER".equals(sesion.getAttribute("rol"))) {
            return "redirect:/login?destino=compra";
        }
        long usuario = 0;
        if (!comoInvitado) {
            usuario = ((Number) sesion.getAttribute("usuarioId")).longValue();
        }
        String invitado = (String) sesion.getAttribute("invitadoId");
        String destino = "redirect:/compra?codigo=" + compra;
        if (usuario > 0) {
            destino = "redirect:/mis-tickets?compra=" + compra;
        }
        if (compraService.compraExiste(compra, usuario, invitado, funcion)) {
            return destino;
        }
        if (!formularioValido(funcion, compra, csrf, sesion)) {
            respuesta.setStatus(403);
            modelo.addAttribute("mensaje", "Este formulario ya no es válido. Vuelve a abrir la función.");
            mostrarSala(funcion, compra, 1, sesion, respuesta, modelo);
            return "sala";
        }
        Integer cantidad = (Integer) sesion.getAttribute("cantidadPendiente");
        if (cantidad == null || cantidad < 1) {
            respuesta.setStatus(400);
            modelo.addAttribute("mensaje", "Selecciona al menos una entrada para comprar.");
            mostrarSala(funcion, compra, 1, sesion, respuesta, modelo);
            return "sala";
        }
        try {
            compraService.registrarCompra(compra, usuario, invitado, funcion, cantidad);
        } catch (IllegalArgumentException error) {
            respuesta.setStatus(409);
            modelo.addAttribute("mensaje", error.getMessage());
            mostrarSala(funcion, compra, cantidad, sesion, respuesta, modelo);
            return "sala";
        } catch (DuplicateKeyException error) {
            if (compraService.compraExiste(compra, usuario, invitado, funcion)) {
                return destino;
            }
            respuesta.setStatus(409);
            modelo.addAttribute("mensaje", "Otra compra acaba de tomar esos turnos. Inténtalo de nuevo.");
            mostrarSala(funcion, compra, cantidad, sesion, respuesta, modelo);
            return "sala";
        }
        sesion.removeAttribute("cantidadPendiente");
        return destino;
    }

    @GetMapping("/compra")
    public String comprobante(@RequestParam(defaultValue = "") String codigo, HttpSession sesion,
                              HttpServletResponse respuesta, Model modelo) {
        Map<String, Object> compra = ticketService.comprobanteInvitado(codigo, sesion.getAttribute("invitadoId"));
        if (compra == null) {
            respuesta.setStatus(404);
            modelo.addAttribute("mensaje", "Este comprobante no está disponible en tu sesión.");
        } else {
            modelo.addAttribute("compraConfirmada", compra);
            modelo.addAttribute("tickets", ticketService.ticketsDeCompra(codigo));
        }
        return "compra";
    }

    private boolean formularioValido(int funcion, String compra, String csrf, HttpSession sesion) {
        return !csrf.isEmpty() && csrf.equals(sesion.getAttribute("csrf"))
                && !compra.isEmpty() && compra.equals(sesion.getAttribute("compraToken"))
                && Integer.valueOf(funcion).equals(sesion.getAttribute("funcionCompra"));
    }

    // Pide los datos de la función al servicio y los pasa a la vista con addAttribute.
    // Devuelve null (y responde 404) si la función no está disponible.
    private Map<String, Object> mostrarSala(int funcion, String compra, int cantidad, HttpSession sesion,
                                            HttpServletResponse respuesta, Model modelo) {
        Map<String, Object> sala = carteleraService.prepararFuncion(funcion);
        if (sala == null) {
            respuesta.setStatus(404);
            modelo.addAttribute("mensaje", "Esta función no está disponible para comprar.");
            return null;
        }
        int libres = (Integer) sala.get("libres");
        modelo.addAttribute("funcion", sala.get("funcion"));
        modelo.addAttribute("libres", libres);
        modelo.addAttribute("maximo", compraService.maximoSeleccionable(libres));
        modelo.addAttribute("cantidad", cantidad);
        modelo.addAttribute("compra", compra);
        modelo.addAttribute("csrf", sesion.getAttribute("csrf"));
        return sala;
    }
}
