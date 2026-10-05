package com.cineamazonas.service;

import com.cineamazonas.repository.CarteleraRepository;
import com.cineamazonas.repository.UsuarioRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

// Capa de lógica de negocio: datos que muestra el panel de administración.
@Service
public class AdminService {

    private final CarteleraRepository carteleraRepository;
    private final UsuarioRepository usuarioRepository;

    public AdminService(CarteleraRepository carteleraRepository, UsuarioRepository usuarioRepository) {
        this.carteleraRepository = carteleraRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Map<String, Object>> peliculas() {
        return carteleraRepository.listarPeliculasConGenero();
    }

    public List<Map<String, Object>> generos() {
        return carteleraRepository.listarGenerosConCantidad();
    }

    public List<Map<String, Object>> salas() {
        return carteleraRepository.listarSalas();
    }

    public List<Map<String, Object>> funcionesDeHoy() {
        return carteleraRepository.listarFuncionesDeHoyConOcupacion();
    }

    public List<Map<String, Object>> usuarios() {
        return usuarioRepository.listar();
    }
}
