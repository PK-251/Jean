package com.cineamazonas.service;

import com.cineamazonas.repository.UsuarioRepository;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

// Capa de lógica de negocio: validaciones de login y registro, y protección de contraseñas.
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Map<String, Boolean> validarLogin(String correo, String clave) {
        Map<String, Boolean> errores = new LinkedHashMap<>();
        if (!correoValido(correo)) {
            errores.put("login-email", true);
        }
        if (clave.isEmpty() || clave.length() > 128) {
            errores.put("login-password", true);
        }
        return errores;
    }

    public Map<String, Boolean> validarRegistro(String nombre, String correo, String clave, String confirmacion) {
        Map<String, Boolean> errores = new LinkedHashMap<>();
        if (nombre.isEmpty() || nombre.length() > 100) {
            errores.put("reg-name", true);
        }
        if (!correoValido(correo)) {
            errores.put("reg-email", true);
        }
        if (clave.length() < 8 || clave.length() > 128) {
            errores.put("reg-password", true);
        }
        if (confirmacion.isEmpty() || !clave.equals(confirmacion)) {
            errores.put("reg-confirm", true);
        }
        return errores;
    }

    // Devuelve el usuario si las credenciales son correctas, o null si no lo son.
    public Map<String, Object> autenticar(String correo, String clave) {
        List<Map<String, Object>> usuarios = usuarioRepository.buscarActivoPorCorreo(correo);
        if (usuarios.isEmpty() || !verificarClave(clave, (String) usuarios.get(0).get("clave"))) {
            return null;
        }
        return usuarios.get(0);
    }

    // Devuelve false si el correo ya estaba registrado.
    public boolean registrar(String nombre, String correo, String clave) {
        try {
            usuarioRepository.insertar(nombre, correo, guardarClave(clave));
        } catch (DuplicateKeyException error) {
            return false;
        }
        return true;
    }

    private boolean correoValido(String correo) {
        return correo.length() <= 254 && correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");
    }

    private String guardarClave(String clave) {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        return HexFormat.of().formatHex(sal) + ":" + HexFormat.of().formatHex(resumen(clave, sal));
    }

    private boolean verificarClave(String clave, String almacenada) {
        String[] partes = almacenada.split(":");
        if (partes.length != 2) {
            return false;
        }
        try {
            byte[] sal = HexFormat.of().parseHex(partes[0]);
            byte[] esperado = HexFormat.of().parseHex(partes[1]);
            return MessageDigest.isEqual(esperado, resumen(clave, sal));
        } catch (IllegalArgumentException error) {
            return false;
        }
    }

    private byte[] resumen(String clave, byte[] sal) {
        PBEKeySpec parametros = new PBEKeySpec(clave.toCharArray(), sal, 120000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(parametros).getEncoded();
        } catch (GeneralSecurityException error) {
            throw new IllegalStateException("No se pudo proteger la contraseña", error);
        } finally {
            parametros.clearPassword();
        }
    }
}
