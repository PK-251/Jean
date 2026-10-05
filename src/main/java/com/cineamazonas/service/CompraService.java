package com.cineamazonas.service;

import com.cineamazonas.repository.CarteleraRepository;
import com.cineamazonas.repository.TicketRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Capa de lógica de negocio: reglas de la compra de entradas.
// El cliente compra solo la cantidad de entradas; no elige asiento. Cada entrada recibe un
// número de turno correlativo y los clientes ingresan a la sala en ese orden.
@Service
public class CompraService {

    public static final int MAXIMO_POR_COMPRA = 10;

    private final CarteleraRepository carteleraRepository;
    private final TicketRepository ticketRepository;

    public CompraService(CarteleraRepository carteleraRepository, TicketRepository ticketRepository) {
        this.carteleraRepository = carteleraRepository;
        this.ticketRepository = ticketRepository;
    }

    // Cuántas entradas se pueden ofrecer en la lista de cantidad.
    public int maximoSeleccionable(int libres) {
        return Math.min(libres, MAXIMO_POR_COMPRA);
    }

    // Lanza IllegalArgumentException si la cantidad no es válida para las entradas libres.
    public void validarCantidad(int cantidad, int libres) {
        if (cantidad < 1) {
            throw new IllegalArgumentException("Selecciona al menos una entrada para continuar.");
        }
        if (cantidad > MAXIMO_POR_COMPRA) {
            throw new IllegalArgumentException("Puedes comprar hasta " + MAXIMO_POR_COMPRA + " entradas por compra.");
        }
        if (cantidad > libres) {
            throw new IllegalArgumentException("No hay suficientes entradas disponibles para esta función.");
        }
    }

    public BigDecimal calcularTotal(BigDecimal precio, int cantidad) {
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }

    public boolean compraExiste(String compra, long usuario, String invitado, int funcion) {
        return ticketRepository.contarCompra(compra, usuario, invitado, funcion) > 0;
    }

    // Guarda la compra completa en una sola transacción: o se guardan todas las entradas, o ninguna.
    // usuario = 0 significa compra de invitado. Si hay un error de datos, la transacción se deshace.
    @Transactional
    public void registrarCompra(String compra, long usuario, String invitado, int funcion, int cantidad) {
        List<Map<String, Object>> funciones = carteleraRepository.buscarFuncionDisponibleBloqueada(funcion);
        if (funciones.isEmpty()) {
            throw new IllegalArgumentException("La función ya no está disponible.");
        }
        int aforo = ((Number) funciones.get(0).get("aforo")).intValue();
        BigDecimal precio = (BigDecimal) funciones.get(0).get("precio");

        List<Map<String, Object>> compras = ticketRepository.buscarCompra(compra);
        if (!compras.isEmpty()) {
            Map<String, Object> existente = compras.get(0);
            long duenio = 0;
            if (existente.get("usuario_id") != null) {
                duenio = ((Number) existente.get("usuario_id")).longValue();
            }
            int funcionExistente = ((Number) existente.get("funcion_id")).intValue();
            boolean otroInvitado = usuario == 0 && (invitado == null || !invitado.equals(existente.get("invitado")));
            if (duenio != usuario || funcionExistente != funcion || otroInvitado) {
                throw new IllegalArgumentException("La compra no corresponde a tu sesión.");
            }
            return;
        }

        // La función está bloqueada: nadie más puede comprar mientras se cuenta la ocupación.
        int ultimo = 0;
        int ocupacion = 0;
        List<Map<String, Object>> tickets = ticketRepository.listarPorFuncion(funcion);
        for (int i = 0; i < tickets.size(); i++) {
            Map<String, Object> ticket = tickets.get(i);
            ultimo = Math.max(ultimo, ((Number) ticket.get("turno")).intValue());
            if (Boolean.TRUE.equals(ticket.get("activo"))) {
                ocupacion++;
            }
        }
        validarCantidad(cantidad, aforo - ocupacion);

        Long usuarioGuardado = null;
        String invitadoGuardado = invitado;
        if (usuario > 0) {
            usuarioGuardado = usuario;
            invitadoGuardado = null;
        }
        BigDecimal total = calcularTotal(precio, cantidad);
        ticketRepository.insertarCompra(compra, usuarioGuardado, invitadoGuardado, funcion, cantidad, total);
        for (int i = 0; i < cantidad; i++) {
            ticketRepository.insertarTicket(usuarioGuardado, funcion, ultimo + i + 1, precio, compra);
        }
    }
}
