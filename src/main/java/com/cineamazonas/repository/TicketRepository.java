package com.cineamazonas.repository;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// Capa de acceso a datos: tickets y compras.
@Repository
public class TicketRepository {

    private final JdbcTemplate bd;

    public TicketRepository(JdbcTemplate bd) {
        this.bd = bd;
    }

    public List<Map<String, Object>> listarPorUsuario(Object usuarioId) {
        return bd.queryForList(
                "SELECT t.*, p.nombre AS pelicula, s.nombre AS sala, "
                + "FORMATDATETIME(f.fecha, 'dd/MM/yyyy') AS fechaTexto, "
                + "FORMATDATETIME(f.hora, 'h:mm a', 'en') AS horaTexto "
                + "FROM tickets t JOIN funciones f ON f.id = t.funcion_id "
                + "JOIN peliculas p ON p.id = f.pelicula_id JOIN salas s ON s.id = f.sala_id "
                + "WHERE t.usuario_id = ? ORDER BY t.id", usuarioId);
    }

    public List<Map<String, Object>> buscarCompraDeUsuario(String compra, Object usuarioId) {
        return bd.queryForList("SELECT cantidad, total FROM compras WHERE id = ? AND usuario_id = ?",
                compra, usuarioId);
    }

    public List<Map<String, Object>> buscarComprobanteInvitado(String codigo, Object invitado) {
        return bd.queryForList(
                "SELECT c.*, p.nombre AS pelicula, s.nombre AS sala, "
                + "FORMATDATETIME(f.fecha, 'dd/MM/yyyy') AS fechaTexto, "
                + "FORMATDATETIME(f.hora, 'h:mm a', 'en') AS horaTexto "
                + "FROM compras c JOIN funciones f ON f.id = c.funcion_id "
                + "JOIN peliculas p ON p.id = f.pelicula_id JOIN salas s ON s.id = f.sala_id "
                + "WHERE c.id = ? AND c.usuario_id IS NULL AND c.invitado = ?",
                codigo, invitado);
    }

    public List<Map<String, Object>> listarPorCompra(String compra) {
        return bd.queryForList("SELECT id, turno, precio FROM tickets WHERE compra_id = ? ORDER BY turno", compra);
    }

    public List<Map<String, Object>> listarPorFuncion(int funcion) {
        return bd.queryForList("SELECT turno, activo FROM tickets WHERE funcion_id = ?", funcion);
    }

    public List<Map<String, Object>> buscarCompra(String compra) {
        return bd.queryForList("SELECT usuario_id, invitado, funcion_id FROM compras WHERE id = ?", compra);
    }

    public int contarCompra(String compra, long usuario, String invitado, int funcion) {
        return bd.queryForObject(
                "SELECT COUNT(*) FROM compras WHERE id = ? AND COALESCE(usuario_id, 0) = ? "
                + "AND (? > 0 OR invitado = ?) AND funcion_id = ?",
                Integer.class, compra, usuario, usuario, invitado, funcion);
    }

    // usuario e invitado pueden ser null: una compra es de una cuenta o de un invitado.
    public void insertarCompra(String compra, Long usuario, String invitado, int funcion,
                               int cantidad, BigDecimal total) {
        bd.update("INSERT INTO compras (id, usuario_id, funcion_id, cantidad, total, invitado) VALUES (?, ?, ?, ?, ?, ?)",
                new Object[] {compra, usuario, funcion, cantidad, total, invitado},
                new int[] {Types.VARCHAR, Types.BIGINT, Types.INTEGER, Types.INTEGER, Types.DECIMAL, Types.VARCHAR});
    }

    // El turno es el número de ingreso a la sala: se atiende en orden de llegada.
    public void insertarTicket(Long usuario, int funcion, int turno, BigDecimal precio, String compra) {
        bd.update("INSERT INTO tickets (usuario_id, funcion_id, turno, precio, fecha_compra, compra_id) "
                + "VALUES (?, ?, ?, ?, CURRENT_DATE, ?)",
                new Object[] {usuario, funcion, turno, precio, compra},
                new int[] {Types.BIGINT, Types.INTEGER, Types.INTEGER, Types.DECIMAL, Types.VARCHAR});
    }
}
