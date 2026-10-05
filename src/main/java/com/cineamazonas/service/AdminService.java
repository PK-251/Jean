package com.cineamazonas.service;

import com.cineamazonas.repository.CarteleraRepository;
import com.cineamazonas.repository.DashboardRepository;
import com.cineamazonas.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

// Capa de lógica de negocio: datos que muestra el panel de administración.
@Service
public class AdminService {

    private final CarteleraRepository carteleraRepository;
    private final UsuarioRepository usuarioRepository;
    private final DashboardRepository dashboardRepository;

    private static final String[] DIAS = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};

    public AdminService(CarteleraRepository carteleraRepository, UsuarioRepository usuarioRepository,
                        DashboardRepository dashboardRepository) {
        this.carteleraRepository = carteleraRepository;
        this.usuarioRepository = usuarioRepository;
        this.dashboardRepository = dashboardRepository;
    }

    // Las 5 métricas puntuales del dashboard, leídas desde H2.
    public Map<String, Object> metricas() {
        Map<String, Object> metricas = new HashMap<>();
        Map<String, Object> ventas = dashboardRepository.ventasDelDia();
        metricas.put("ventasHoy", ventas.get("total"));
        metricas.put("entradasHoy", ventas.get("entradas"));

        List<Map<String, Object>> proxima = dashboardRepository.proximaFuncion();
        if (proxima.isEmpty()) {
            metricas.put("cuposProxima", "—");
            metricas.put("proximaFuncion", "Sin funciones pendientes hoy");
        } else {
            metricas.put("cuposProxima", proxima.get(0).get("cupos"));
            metricas.put("proximaFuncion", proxima.get(0).get("pelicula") + " · " + proxima.get(0).get("horaTexto"));
        }

        Map<String, Object> funciones = dashboardRepository.funcionesDeHoy();
        metricas.put("funcionesHoy", funciones.get("funciones"));
        metricas.put("salasHoy", funciones.get("salas"));
        metricas.put("usuarios", dashboardRepository.contarUsuarios());

        List<Map<String, Object>> masVendida = dashboardRepository.peliculaMasVendidaDelMes();
        if (masVendida.isEmpty()) {
            metricas.put("masVendida", "—");
            metricas.put("masVendidaEntradas", 0);
        } else {
            metricas.put("masVendida", masVendida.get(0).get("pelicula"));
            metricas.put("masVendidaEntradas", masVendida.get(0).get("entradas"));
        }
        return metricas;
    }

    // Ventas de los últimos 7 días (hoy al final). La altura de cada barra es relativa al día con más ventas.
    public List<Map<String, Object>> ventasSemana() {
        Map<LocalDate, BigDecimal> totales = new HashMap<>();
        BigDecimal maximo = BigDecimal.ZERO;
        for (Map<String, Object> fila : dashboardRepository.ventasUltimos7Dias()) {
            LocalDate fecha = ((java.sql.Date) fila.get("fecha")).toLocalDate();
            BigDecimal total = (BigDecimal) fila.get("total");
            totales.put(fecha, total);
            if (total.compareTo(maximo) > 0) {
                maximo = total;
            }
        }

        List<Map<String, Object>> dias = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate fecha = hoy.minusDays(i);
            BigDecimal total = totales.containsKey(fecha) ? totales.get(fecha) : BigDecimal.ZERO;
            int porcentaje = 0;
            if (maximo.signum() > 0) {
                porcentaje = total.multiply(BigDecimal.valueOf(100)).divide(maximo, 0, RoundingMode.HALF_UP).intValue();
            }
            Map<String, Object> dia = new HashMap<>();
            dia.put("nombre", DIAS[fecha.getDayOfWeek().getValue() - 1]);
            dia.put("total", total);
            dia.put("porcentaje", Math.max(porcentaje, 2));
            dias.add(dia);
        }
        return dias;
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
