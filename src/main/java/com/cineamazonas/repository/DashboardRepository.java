package com.cineamazonas.repository;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// Capa de acceso a datos: consultas de las métricas del dashboard administrativo.
@Repository
public class DashboardRepository {

    private final JdbcTemplate bd;

    public DashboardRepository(JdbcTemplate bd) {
        this.bd = bd;
    }

    public Map<String, Object> ventasDelDia() {
        return bd.queryForMap(
                "SELECT COALESCE(SUM(precio), 0) AS total, COUNT(*) AS entradas FROM tickets "
                + "WHERE activo = TRUE AND fecha_compra = CURRENT_DATE");
    }

    // Próxima función de hoy que todavía no empieza, con los cupos que le quedan.
    public List<Map<String, Object>> proximaFuncion() {
        return bd.queryForList(
                "SELECT p.nombre AS pelicula, FORMATDATETIME(f.hora, 'h:mm a', 'en') AS horaTexto, "
                + "s.aforo - (SELECT COUNT(*) FROM tickets t WHERE t.funcion_id = f.id AND t.activo = TRUE) AS cupos "
                + "FROM funciones f JOIN peliculas p ON p.id = f.pelicula_id JOIN salas s ON s.id = f.sala_id "
                + "WHERE f.activo = TRUE AND f.fecha = CURRENT_DATE AND f.hora >= LOCALTIME "
                + "ORDER BY f.hora LIMIT 1");
    }

    public Map<String, Object> funcionesDeHoy() {
        return bd.queryForMap(
                "SELECT COUNT(*) AS funciones, COUNT(DISTINCT sala_id) AS salas FROM funciones "
                + "WHERE activo = TRUE AND fecha = CURRENT_DATE");
    }

    public int contarUsuarios() {
        return bd.queryForObject("SELECT COUNT(*) FROM usuarios WHERE activo = TRUE", Integer.class);
    }

    public List<Map<String, Object>> peliculaMasVendidaDelMes() {
        return bd.queryForList(
                "SELECT p.nombre AS pelicula, COUNT(*) AS entradas FROM tickets t "
                + "JOIN funciones f ON f.id = t.funcion_id JOIN peliculas p ON p.id = f.pelicula_id "
                + "WHERE t.activo = TRUE AND YEAR(t.fecha_compra) = YEAR(CURRENT_DATE) "
                + "AND MONTH(t.fecha_compra) = MONTH(CURRENT_DATE) "
                + "GROUP BY p.nombre ORDER BY entradas DESC, p.nombre LIMIT 1");
    }

    // Importe vendido en cada uno de los últimos 7 días (solo los días con ventas).
    public List<Map<String, Object>> ventasUltimos7Dias() {
        return bd.queryForList(
                "SELECT fecha_compra AS fecha, SUM(precio) AS total FROM tickets "
                + "WHERE activo = TRUE AND fecha_compra > DATEADD('DAY', -7, CURRENT_DATE) "
                + "GROUP BY fecha_compra");
    }
}
