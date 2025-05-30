package co.edu.unbosque.utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import co.edu.unbosque.entity.DetalleVenta;
import co.edu.unbosque.entity.MetodoPago;
import co.edu.unbosque.entity.Producto;
import co.edu.unbosque.entity.Transaccion;
import co.edu.unbosque.entity.Venta;

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
    
    public void enviarRecuperacionClave(String destinatario, String claveTemporal) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Recuperación de contraseña");
        mensaje.setText(
            "Hola,\n\n"
            + "Hemos recibido una solicitud para restablecer tu contraseña.\n"
            + "Tu nueva clave temporal es: " + claveTemporal + "\n\n"
            + "Te recomendamos cambiarla después de iniciar sesión.\n"
            + "\nSi no solicitaste este cambio, puedes ignorar este correo."
        );
        mailSender.send(mensaje);
    }
    
    public void enviarResumenCompra(
    	    String destinatario,
    	    String nombreCliente,
    	    Venta venta,
    	    List<DetalleVenta> detalles,
    	    List<Producto> productos,
    	    Transaccion transaccion,
    	    MetodoPago metodoPago
    	) {
    	    StringBuilder cuerpo = new StringBuilder();
    	    cuerpo.append("¡Hola ").append(nombreCliente).append("!\n\n");
    	    cuerpo.append("Gracias por tu compra en Delta Music. Aquí tienes el resumen de tu pedido:\n\n");
    	    cuerpo.append("Fecha de la compra: ").append(venta.getFechaVenta()).append("\n");
    	    cuerpo.append("ID de la venta: ").append(venta.getId()).append("\n\n");
    	    cuerpo.append("Productos:\n");
    	    cuerpo.append(String.format("%-25s %-10s %-12s %-10s\n", "Nombre", "Cantidad", "V. Unitario", "Subtotal"));
    	    cuerpo.append("-------------------------------------------------------------\n");

    	    int totalSinIva = 0;
    	    for (DetalleVenta detalle : detalles) {
    	        Producto prod = productos.stream()
    	            .filter(p -> p.getId().intValue() == detalle.getIdProducto())
    	            .findFirst()
    	            .orElse(null);

    	        String nombreProd = (prod != null) ? prod.getDescripcion() : "Producto desconocido";
    	        int cantidad = detalle.getCantComp();
    	        int valorUnit = detalle.getValorUnit();
    	        int subtotal = cantidad * valorUnit;
    	        totalSinIva += subtotal;

    	        cuerpo.append(String.format("%-25s %-10d %-12d %-10d\n", nombreProd, cantidad, valorUnit, subtotal));
    	    }

    	    cuerpo.append("\nTotal de la compra (sin IVA): ").append(totalSinIva).append(" COP\n");
    	    cuerpo.append("IVA aplicado: ").append(venta.getValorIva()).append(" COP\n");
    	    cuerpo.append("TOTAL A PAGAR: ").append(totalSinIva + venta.getValorIva()).append(" COP\n\n");

    	    cuerpo.append("---- Datos de la transacción ----\n");
    	    if (transaccion != null && metodoPago != null) {
    	        cuerpo.append("ID Transacción: ").append(transaccion.getId()).append("\n");
    	        cuerpo.append("Fecha y hora: ").append(transaccion.getFechaHora()).append("\n");
    	        cuerpo.append("Banco: ").append(transaccion.getIdBanco() != null ? transaccion.getIdBanco() : "N/A").append("\n");
    	        cuerpo.append("Método de pago: ").append(metodoPago.getDescripcion()).append("\n");
    	        cuerpo.append("Valor pagado: ").append(transaccion.getValorTx()).append(" COP\n");
    	        if (transaccion.getNumTarjeta() != null)
    	            cuerpo.append("Número de tarjeta: **** ").append(transaccion.getNumTarjeta().substring(Math.max(transaccion.getNumTarjeta().length() - 4, 0))).append("\n");
    	    } else {
    	        cuerpo.append("Transacción/Pago: N/A\n");
    	    }

    	    cuerpo.append("\n¡Gracias por preferirnos!\nDelta Music");

    	    SimpleMailMessage mensaje = new SimpleMailMessage();
    	    mensaje.setTo(destinatario);
    	    mensaje.setSubject("Resumen de tu compra en Delta Music");
    	    mensaje.setText(cuerpo.toString());
    	    mailSender.send(mensaje);
    	}



}
