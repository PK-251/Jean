package com.cineamazonas.service;

import com.cineamazonas.repository.TicketRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

// Capa de lógica de negocio: consulta de tickets y comprobantes.
@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    // Tickets de una cuenta, con el número formateado y el estado en texto.
    public List<Map<String, Object>> ticketsDeUsuario(Object usuarioId) {
        List<Map<String, Object>> tickets = ticketRepository.listarPorUsuario(usuarioId);
        for (int i = 0; i < tickets.size(); i++) {
            Map<String, Object> ticket = tickets.get(i);
            long numero = ((Number) ticket.get("id")).longValue();
            ticket.put("numero", String.format("%03d", numero));
            if (Boolean.TRUE.equals(ticket.get("activo"))) {
                ticket.put("estado", "Vigente");
            } else {
                ticket.put("estado", "Anulado");
            }
        }
        return tickets;
    }

    // Devuelve la compra de esa cuenta, o null si no existe.
    public Map<String, Object> compraDeUsuario(String compra, Object usuarioId) {
        List<Map<String, Object>> compras = ticketRepository.buscarCompraDeUsuario(compra, usuarioId);
        if (compras.isEmpty()) {
            return null;
        }
        return compras.get(0);
    }

    // Devuelve la compra del invitado de esta sesión, o null si no le pertenece.
    public Map<String, Object> comprobanteInvitado(String codigo, Object invitado) {
        List<Map<String, Object>> compras = ticketRepository.buscarComprobanteInvitado(codigo, invitado);
        if (compras.isEmpty()) {
            return null;
        }
        return compras.get(0);
    }

    public List<Map<String, Object>> ticketsDeCompra(String codigo) {
        return ticketRepository.listarPorCompra(codigo);
    }
}
