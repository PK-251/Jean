package com.cineamazonas.service;

import com.cineamazonas.repository.CarteleraRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

// Capa de lógica de negocio: cartelera y disponibilidad de entradas de una función.
@Service
public class CarteleraService {

    private final CarteleraRepository carteleraRepository;

    public CarteleraService(CarteleraRepository carteleraRepository) {
        this.carteleraRepository = carteleraRepository;
    }

    public List<Map<String, Object>> peliculasActivas() {
        return carteleraRepository.listarPeliculasActivas();
    }

    public List<Map<String, Object>> funcionesDeHoy() {
        return carteleraRepository.listarFuncionesDeHoy();
    }

    // Datos de la función para la pantalla de compra. Devuelve null si la función no se puede comprar.
    // Claves del resultado: funcion y libres (entradas todavía disponibles según el aforo).
    public Map<String, Object> prepararFuncion(int funcion) {
        List<Map<String, Object>> funciones = carteleraRepository.buscarFuncionDisponible(funcion);
        if (funciones.isEmpty()) {
            return null;
        }
        Map<String, Object> datos = funciones.get(0);
        int aforo = ((Number) datos.get("aforo")).intValue();
        int entradas = carteleraRepository.contarEntradasActivas(funcion);
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("funcion", datos);
        resultado.put("libres", Math.max(0, aforo - entradas));
        return resultado;
    }
}
