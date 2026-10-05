package com.cineamazonas.controller;

import com.cineamazonas.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(defaultValue = "login") String panel,
                        @RequestParam(defaultValue = "cartelera") String destino,
                        @RequestParam(defaultValue = "") String registro,
                        HttpSession sesion, Model modelo) {
        if (!"register".equals(panel)) {
            panel = "login";
        }
        preparar(modelo, sesion, panel, destino, "", "", new LinkedHashMap<>());
        if ("ok".equals(registro)) {
            modelo.addAttribute("mensaje", "Cuenta creada. Ya puedes ingresar.");
        }
        return "login";
    }

    @GetMapping("/admin/login")
    public String loginAdmin(HttpSession sesion, Model modelo) {
        preparar(modelo, sesion, "login", "cartelera", "", "", new LinkedHashMap<>());
        return "admin-login";
    }

    @PostMapping("/invitado")
    public String invitado(@RequestParam(defaultValue = "cartelera") String destino,
                           @RequestParam(defaultValue = "") String csrf, HttpSession sesion,
                           HttpServletResponse respuesta, Model modelo) {
        if (!csrfValido(sesion, csrf)) {
            respuesta.setStatus(403);
            preparar(modelo, sesion, "login", destino, "", "", new LinkedHashMap<>());
            modelo.addAttribute("mensaje", "El formulario venció. Vuelve a intentarlo.");
            return "login";
        }
        if ("compra".equals(destino) && sesion.getAttribute("cantidadPendiente") != null) {
            sesion.setAttribute("compraInvitado", true);
            return "redirect:/confirmar-compra";
        }
        if ("sala".equals(destino) && sesion.getAttribute("funcionPendiente") != null) {
            return "redirect:/sala?funcion=" + sesion.getAttribute("funcionPendiente");
        }
        return "redirect:/cartelera";
    }

    @PostMapping({"/login", "/admin/login"})
    public String ingresar(@RequestParam Map<String, String> datos, HttpServletRequest solicitud,
                           HttpServletResponse respuesta, HttpSession sesion, Model modelo) {
        String correo = texto(datos, "email").trim().toLowerCase(Locale.ROOT);
        String clave = texto(datos, "password");
        String destino = texto(datos, "destino");
        preparar(modelo, sesion, "login", destino, correo, "", new LinkedHashMap<>());
        String vista = "login";
        String rolPermitido = "USER";
        if (solicitud.getRequestURI().equals(solicitud.getContextPath() + "/admin/login")) {
            vista = "admin-login";
            rolPermitido = "ADMIN";
        }
        if (!csrfValido(sesion, texto(datos, "csrf"))) {
            respuesta.setStatus(403);
            modelo.addAttribute("mensaje", "El formulario venció. Vuelve a intentarlo.");
            return vista;
        }
        Map<String, Boolean> errores = authService.validarLogin(correo, clave);
        if (!errores.isEmpty()) {
            modelo.addAttribute("errores", errores);
            return vista;
        }
        Map<String, Object> usuario = authService.autenticar(correo, clave);
        if (usuario == null) {
            modelo.addAttribute("mensaje", "Correo o contraseña incorrectos.");
            return vista;
        }
        if (!rolPermitido.equals(usuario.get("rol"))) {
            respuesta.setStatus(403);
            modelo.addAttribute("mensaje", "Esta cuenta no corresponde a este acceso. Usa el login de tu rol.");
            return vista;
        }
        solicitud.changeSessionId();
        sesion.setAttribute("usuarioId", usuario.get("id"));
        sesion.setAttribute("nombre", usuario.get("nombre"));
        sesion.setAttribute("rol", usuario.get("rol"));
        sesion.setAttribute("csrf", UUID.randomUUID().toString());
        int duracion = 1800;
        int duracionCookie = -1;
        if (datos.containsKey("remember")) {
            duracion = 604800;
            duracionCookie = duracion;
        }
        sesion.setMaxInactiveInterval(duracion);
        Cookie cookie = new Cookie("JSESSIONID", sesion.getId());
        String ruta = solicitud.getContextPath();
        if (ruta.isEmpty()) {
            ruta = "/";
        }
        cookie.setPath(ruta);
        cookie.setHttpOnly(true);
        cookie.setSecure(solicitud.isSecure());
        cookie.setAttribute("SameSite", "Lax");
        cookie.setMaxAge(duracionCookie);
        respuesta.addCookie(cookie);
        if ("ADMIN".equals(usuario.get("rol"))) {
            return "redirect:/admin";
        }
        if ("tickets".equals(destino)) {
            return "redirect:/mis-tickets";
        }
        if ("compra".equals(destino) && sesion.getAttribute("cantidadPendiente") != null) {
            sesion.removeAttribute("compraInvitado");
            return "redirect:/confirmar-compra";
        }
        if ("sala".equals(destino) && sesion.getAttribute("funcionPendiente") != null) {
            return "redirect:/sala?funcion=" + sesion.getAttribute("funcionPendiente");
        }
        return "redirect:/cartelera";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam Map<String, String> datos, HttpSession sesion,
                            HttpServletResponse respuesta, Model modelo) {
        String nombre = texto(datos, "nombre").trim();
        String correo = texto(datos, "email").trim().toLowerCase(Locale.ROOT);
        String clave = texto(datos, "password");
        String confirmacion = texto(datos, "confirmPassword");
        preparar(modelo, sesion, "register", texto(datos, "destino"), correo, nombre, new LinkedHashMap<>());
        if (!csrfValido(sesion, texto(datos, "csrf"))) {
            respuesta.setStatus(403);
            modelo.addAttribute("mensaje", "El formulario venció. Vuelve a intentarlo.");
            return "login";
        }
        Map<String, Boolean> errores = authService.validarRegistro(nombre, correo, clave, confirmacion);
        modelo.addAttribute("errores", errores);
        if (!errores.isEmpty()) {
            return "login";
        }
        if (!authService.registrar(nombre, correo, clave)) {
            errores.put("reg-email", true);
            modelo.addAttribute("mensaje", "Ese correo ya está registrado.");
            return "login";
        }
        if ("sala".equals(texto(datos, "destino")) || "compra".equals(texto(datos, "destino"))) {
            return "redirect:/login?registro=ok&destino=" + texto(datos, "destino");
        }
        return "redirect:/login?registro=ok";
    }

    @PostMapping("/salir")
    public String salir(@RequestParam(defaultValue = "") String csrf, HttpSession sesion,
                        HttpServletResponse respuesta) throws IOException {
        if (!csrfValido(sesion, csrf)) {
            respuesta.sendError(403);
            return null;
        }
        String destino = "redirect:/login";
        if ("ADMIN".equals(sesion.getAttribute("rol"))) {
            destino = "redirect:/admin/login";
        }
        sesion.invalidate();
        return destino;
    }

    private static void preparar(Model modelo, HttpSession sesion, String panel, String destino,
                                 String correo, String nombre, Map<String, Boolean> errores) {
        switch (destino) {
            case "tickets":
            case "sala":
            case "compra":
                break;
            default:
                destino = "cartelera";
                break;
        }
        if (sesion.getAttribute("csrf") == null) {
            sesion.setAttribute("csrf", UUID.randomUUID().toString());
        }
        modelo.addAttribute("panel", panel);
        modelo.addAttribute("destino", destino);
        modelo.addAttribute("email", correo);
        modelo.addAttribute("nombre", nombre);
        modelo.addAttribute("errores", errores);
        modelo.addAttribute("csrf", sesion.getAttribute("csrf"));
    }

    private static String texto(Map<String, String> datos, String campo) {
        String valor = datos.get(campo);
        if (valor == null) {
            return "";
        }
        return valor;
    }

    private static boolean csrfValido(HttpSession sesion, String token) {
        return token != null && token.equals(sesion.getAttribute("csrf"));
    }
}
