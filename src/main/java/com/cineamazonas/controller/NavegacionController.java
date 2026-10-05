package com.cineamazonas.controller;

import com.cineamazonas.service.TicketService;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class NavegacionController {

    private final TicketService ticketService;

    public NavegacionController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/cartelera";
    }

    @GetMapping("/mis-tickets")
    public String misTickets(@RequestParam(defaultValue = "") String compra, HttpSession sesion, Model modelo) {
        if (!"USER".equals(sesion.getAttribute("rol"))) {
            return "redirect:/login?destino=tickets";
        }
        Object usuarioId = sesion.getAttribute("usuarioId");
        modelo.addAttribute("tickets", ticketService.ticketsDeUsuario(usuarioId));
        Map<String, Object> compraConfirmada = ticketService.compraDeUsuario(compra, usuarioId);
        if (compraConfirmada != null) {
            modelo.addAttribute("compraConfirmada", compraConfirmada);
        }
        return "mis-tickets";
    }

    @GetMapping("/publicidad")
    public String publicidad() {
        return "publicidad";
    }

    @GetMapping("/contacto")
    public String contacto() {
        return "contacto";
    }
}
