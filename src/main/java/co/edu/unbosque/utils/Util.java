package co.edu.unbosque.utils;

import org.springframework.stereotype.Component;

import co.edu.unbosque.entity.Usuario;
import jakarta.servlet.http.HttpServletRequest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class Util {

    // Método que genera hash usando usuario + clave (normalizando login)
    public String generarHash(Usuario usuario, String clave) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Normalizar login a minúsculas y sin espacios al inicio o final
            String loginNormalizado = usuario.getLoginUsrio().toLowerCase().trim();
            String texto = loginNormalizado + clave;
            byte[] hashBytes = md.digest(texto.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generando hash", e);
        }
    }

    // Sobrecarga: genera hash solo con la clave (útil para login o pruebas)
    public String generarHash(String clave) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(clave.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generando hash", e);
        }
    }

    public static String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
