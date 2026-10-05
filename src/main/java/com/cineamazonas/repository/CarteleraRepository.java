package com.cineamazonas.repository;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// Capa de acceso a datos: películas, géneros, salas y funciones.
@Repository
public class CarteleraRepository {

    private final JdbcTemplate bd;

    public CarteleraRepository(JdbcTemplate bd) {
        this.bd = bd;
    }

    public List<Map<String, Object>> listarPeliculasActivas() {
        return bd.queryForList(
                "SELECT p.*, g.nombre AS genero FROM peliculas p "
                + "JOIN generos g ON g.id = p.genero_id WHERE p.activo = TRUE AND g.activo = TRUE ORDER BY p.id");
    }

    public List<Map<String, Object>> listarFuncionesDeHoy() {
        return bd.queryForList(
                "SELECT f.*, s.nombre AS sala, FORMATDATETIME(f.hora, 'h:mm a', 'en') AS horaTexto "
                + "FROM funciones f JOIN salas s ON s.id = f.sala_id "
                + "WHERE f.activo = TRUE AND s.activo = TRUE AND f.fecha = CURRENT_DATE ORDER BY f.hora");
    }

    public List<Map<String, Object>> buscarFuncionDisponible(int funcion) {
        return bd.queryForList(
                "SELECT f.*, p.nombre AS pelicula, s.nombre AS sala, s.aforo, "
                + "FORMATDATETIME(f.fecha, 'dd/MM/yyyy') AS fechaTexto, "
                + "FORMATDATETIME(f.hora, 'h:mm a', 'en') AS horaTexto "
                + "FROM funciones f JOIN peliculas p ON p.id = f.pelicula_id "
                + "JOIN generos g ON g.id = p.genero_id JOIN salas s ON s.id = f.sala_id "
                + "WHERE f.id = ? AND f.activo AND p.activo AND g.activo AND s.activo AND f.fecha >= CURRENT_DATE",
                funcion);
    }

    // Igual que la anterior, pero bloquea la función hasta terminar la transacción de compra.
    public List<Map<String, Object>> buscarFuncionDisponibleBloqueada(int funcion) {
        return bd.queryForList(
                "SELECT f.*, s.aforo FROM funciones f JOIN peliculas p ON p.id = f.pelicula_id "
                + "JOIN generos g ON g.id = p.genero_id JOIN salas s ON s.id = f.sala_id "
                + "WHERE f.id = ? AND f.activo AND p.activo AND g.activo AND s.activo AND f.fecha >= CURRENT_DATE "
                + "FOR UPDATE", funcion);
    }

    public int contarEntradasActivas(int funcion) {
        return bd.queryForObject("SELECT COUNT(*) FROM tickets WHERE funcion_id = ? AND activo = TRUE",
                Integer.class, funcion);
    }

    // Consultas del panel de administración.

    public List<Map<String, Object>> listarPeliculasConGenero() {
        return bd.queryForList(
                "SELECT p.*, g.nombre AS genero FROM peliculas p JOIN generos g ON g.id = p.genero_id ORDER BY p.id");
    }

    public List<Map<String, Object>> listarGenerosConCantidad() {
        return bd.queryForList(
                "SELECT g.*, (SELECT COUNT(*) FROM peliculas p WHERE p.genero_id = g.id) AS cantidad "
                + "FROM generos g ORDER BY g.nombre");
    }

    public List<Map<String, Object>> listarSalas() {
        return bd.queryForList("SELECT * FROM salas ORDER BY id");
    }

    public List<Map<String, Object>> listarFuncionesDeHoyConOcupacion() {
        return bd.queryForList(
                "SELECT f.*, p.nombre AS pelicula, s.nombre AS sala, s.aforo, "
                + "FORMATDATETIME(f.hora, 'h:mm a', 'en') AS horaTexto, "
                + "(SELECT COUNT(*) FROM tickets t WHERE t.funcion_id = f.id AND t.activo = TRUE) AS ocupacion "
                + "FROM funciones f JOIN peliculas p ON p.id = f.pelicula_id JOIN salas s ON s.id = f.sala_id "
                + "WHERE f.fecha = CURRENT_DATE ORDER BY f.hora");
    }
}
