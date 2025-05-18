package co.edu.unbosque.utils;

import org.springframework.stereotype.Component;

import co.edu.unbosque.entity.Usuario;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class Util {

    public String generarHash(Usuario usuario, String clave) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Combina el login del usuario con la clave para hacer un hash único
            String texto = usuario.getLoginUsrio() + clave;
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
}
