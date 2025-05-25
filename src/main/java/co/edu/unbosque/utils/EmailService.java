package co.edu.unbosque.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarClaveTemporal(String destinatario, String claveTemporal) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Clave temporal para tu cuenta");
        mensaje.setText("¡Bienvenido!\n\nTu clave temporal para ingresar al sistema es: " + claveTemporal + 
            "\n\nTe recomendamos cambiarla después de tu primer ingreso.");

        mailSender.send(mensaje);
    }
}
