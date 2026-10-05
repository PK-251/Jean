package com.cineamazonas.repository;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// Capa de acceso a datos: solo consultas SQL sobre la tabla usuarios.
@Repository
public class UsuarioRepository {

    private final JdbcTemplate bd;

    public UsuarioRepository(JdbcTemplate bd) {
        this.bd = bd;
    }

    public List<Map<String, Object>> buscarActivoPorCorreo(String correo) {
        return bd.queryForList(
                "SELECT id, nombre, clave, rol FROM usuarios WHERE correo = ? AND activo = TRUE", correo);
    }

    // Lanza DuplicateKeyException si el correo ya existe.
    public void insertar(String nombre, String correo, String clave) {
        bd.update("INSERT INTO usuarios (nombre, correo, clave, rol) VALUES (?, ?, ?, 'USER')",
                nombre, correo, clave);
    }

    public List<Map<String, Object>> listar() {
        return bd.queryForList("SELECT nombre, correo, rol, activo FROM usuarios ORDER BY id");
    }
}
