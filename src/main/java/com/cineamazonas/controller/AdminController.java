package com.cineamazonas.controller;

import com.cineamazonas.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin")
    public String panel(HttpSession sesion, Model modelo) {
        if (sesion.getAttribute("usuarioId") == null || !"ADMIN".equals(sesion.getAttribute("rol"))) {
            return "redirect:/admin/login";
        }
        modelo.addAttribute("metricas", adminService.metricas());
        modelo.addAttribute("ventasSemana", adminService.ventasSemana());
        modelo.addAttribute("peliculas", adminService.peliculas());
        modelo.addAttribute("generos", adminService.generos());
        modelo.addAttribute("salas", adminService.salas());
        modelo.addAttribute("funciones", adminService.funcionesDeHoy());
        modelo.addAttribute("usuarios", adminService.usuarios());
        return "admin";
    }
}
