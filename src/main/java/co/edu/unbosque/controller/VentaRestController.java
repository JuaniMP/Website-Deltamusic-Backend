package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.DetalleVenta;
import co.edu.unbosque.entity.MetodoPago;
import co.edu.unbosque.entity.Parametro;
import co.edu.unbosque.entity.Producto;
import co.edu.unbosque.entity.Transaccion;
import co.edu.unbosque.entity.Usuario;
import co.edu.unbosque.entity.Venta;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.DetalleVentaServiceAPI;
import co.edu.unbosque.service.api.MetodoPagoServiceAPI;
import co.edu.unbosque.service.api.ParametroServiceAPI;
import co.edu.unbosque.service.api.ProductoServiceAPI;
import co.edu.unbosque.service.api.TransaccionServiceAPI;
import co.edu.unbosque.service.api.UsuarioServiceAPI;
import co.edu.unbosque.service.api.VentaServiceAPI;
import co.edu.unbosque.utils.EmailService;
import co.edu.unbosque.utils.JwtUtil;
import co.edu.unbosque.utils.ResourceNotFoundException;
import co.edu.unbosque.utils.Util;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("/venta")
public class VentaRestController {

	@Autowired
	private VentaServiceAPI ventaServiceAPI;

	@Autowired
	private EmailService emailService;

	@Autowired
	private UsuarioServiceAPI usuarioServiceAPI;

	@Autowired
	private ParametroServiceAPI parametroServiceAPI;

	@Autowired
	private MetodoPagoServiceAPI metodoPagoServiceAPI;

	@Autowired
	private TransaccionServiceAPI transaccionServiceAPI;

	@Autowired
	private DetalleVentaServiceAPI detalleVentaServiceAPI;

	@Autowired
	private AuditoriaServiceAPI auditoriaServiceAPI;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private ProductoServiceAPI productoServiceAPI;

	@GetMapping(value = "/getAll")
	public List<Venta> getAll() {
		return ventaServiceAPI.getAll();
	}

	@PostMapping(value = "/saveVenta")
	public ResponseEntity<?> save(@RequestBody VentaRequest ventaRequest, HttpServletRequest request) {
	    // Validación del DTO y sus campos
	    if (ventaRequest == null || ventaRequest.venta == null || ventaRequest.transaccion == null) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body("Debe enviar tanto la venta como la transacción.");
	    }

	    Venta venta = ventaRequest.venta;
	    Transaccion transaccion = ventaRequest.transaccion;
	    String accionAuditoria = "I"; // Por defecto inserción

	    // Asigna la fecha actual si no viene en el body
	    if (venta.getFechaVenta() == null) {
	        venta.setFechaVenta(new java.util.Date());
	    }

	    // Si es PSE o método sin tarjeta, pon valores "NA" o "" por defecto
	    if (transaccion.getIdFranquicia() == null) {
	        transaccion.setIdFranquicia("NA");
	    }
	    if (transaccion.getNumTarjeta() == null) {
	        transaccion.setNumTarjeta("NA");
	    }

	    // 1. Obtener el IVA desde la tabla de parámetros (activo)
	    Parametro ivaParametro = parametroServiceAPI.findByDescripcionAndEstado("IVA", (byte) 1);
	    if (ivaParametro == null) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("No se encontró el parámetro de IVA en la base de datos");
	    }
	    int porcentajeIva = ivaParametro.getValorNumero(); // Ej: 19
	    venta.setValorIva(porcentajeIva); // Guarda el porcentaje en la venta
	    double ivaDecimal = porcentajeIva / 100.0;

	    // 2. Validar máximo 3 productos por día
	    int productosYaComprados = detalleVentaServiceAPI.totalProductosClientePorFecha(venta.getIdCliente(),
	            venta.getFechaVenta());

	    // 3. Procesar detalles y calcular totales
	    int cantidadEnEstaVenta = 0;
	    java.math.BigDecimal totalVenta = java.math.BigDecimal.ZERO;
	    int totalIvaVenta = 0;
	    List<Producto> productosCorreo = new java.util.ArrayList<>();
	    List<DetalleVenta> detallesCorreo = new java.util.ArrayList<>();

	    // *** AJUSTE DE EXISTENCIA Y VALIDACIÓN ***
	    if (venta.getDetalles() != null) {
	        for (DetalleVenta detalle : venta.getDetalles()) {
	            cantidadEnEstaVenta += detalle.getCantComp();
	            Producto producto = productoServiceAPI.get((long) detalle.getIdProducto());
	            if (producto != null) {
	                // VALIDAR EXISTENCIA DISPONIBLE
	                if (producto.getExistencia() < detalle.getCantComp()) {
	                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                            .body("No hay suficiente existencia para el producto: " + producto.getDescripcion()
	                                    + " (Existencia disponible: " + producto.getExistencia() + ")");
	                }
	                // DESCONTAR EXISTENCIA
	                producto.setExistencia(producto.getExistencia() - detalle.getCantComp());
	                productoServiceAPI.save(producto);

	                productosCorreo.add(producto);
	                detallesCorreo.add(detalle);

	                int precioUnit = producto.getPrecioVentaActual().intValue();
	                int subtotal = precioUnit * detalle.getCantComp();

	                int ivaDetalle = 0;
	                if (producto.getTieneIva() == 1) {
	                    ivaDetalle = (int) Math.round(subtotal * ivaDecimal);
	                }
	                detalle.setValorUnit(precioUnit);
	                detalle.setValorIva(ivaDetalle);

	                totalVenta = totalVenta.add(producto.getPrecioVentaActual()
	                        .multiply(java.math.BigDecimal.valueOf(detalle.getCantComp())));
	                totalIvaVenta += ivaDetalle;
	            } else {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                        .body("Producto con ID " + detalle.getIdProducto() + " no existe.");
	            }
	            detalle.setVenta(venta);
	        }
	    }

	    if ((productosYaComprados + cantidadEnEstaVenta) > 3) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body("Solo puedes comprar máximo 3 productos por día.");
	    }

	    venta.setValorVenta(totalVenta.intValue());
	    venta.setValorIva(totalIvaVenta);

	    if (venta.getId() != null) {
	        Venta existente = ventaServiceAPI.get(venta.getId());
	        if (existente != null) {
	            accionAuditoria = "U"; // Actualización
	        }
	    }

	    // Guarda la venta
	    Venta obj = ventaServiceAPI.save(venta);

	    // --- PROCESAR TRANSACCIÓN ---
	    // Validar método de pago
	    MetodoPago metodoPago = metodoPagoServiceAPI.get((long) transaccion.getIdMetodoPago());
	    if (metodoPago == null) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Método de pago no válido.");
	    }

	    // Asocia la venta a la transacción
	    transaccion.setIdCompra(obj.getId().intValue());
	    transaccion.setValorTx(obj.getValorVenta() + obj.getValorIva());
	    transaccion.setFechaHora(new java.util.Date());

	    Transaccion transaccionGuardada = transaccionServiceAPI.save(transaccion);

	    // Auditoría
	    String correoUsuario = getCorreoFromRequest(request);
	    Auditoria aud = new Auditoria();
	    aud.setTablaAccion("venta");
	    aud.setAccionAudtria(accionAuditoria);
	    aud.setUsrioAudtria(correoUsuario);
	    aud.setIdTabla(obj.getId());
	    aud.setComentarioAudtria(
	            (accionAuditoria.equals("I") ? "Creación" : "Actualización") + " de venta con ID " + obj.getId());
	    aud.setFchaAudtria(new Date());
	    aud.setAddressAudtria(Util.getClientIp(request));
	    auditoriaServiceAPI.save(aud);

	    // --- Enviar correo de resumen de compra al usuario ---
	    Usuario usuario = usuarioServiceAPI.get((long) venta.getIdCliente());
	    if (usuario != null) {
	        String nombreCliente = usuario.getLoginUsrio();
	        emailService.enviarResumenCompra(usuario.getCorreoUsuario(), nombreCliente, obj, // venta guardada
	                detallesCorreo, productosCorreo, transaccionGuardada, metodoPago);
	    }

	    return new ResponseEntity<>(obj, HttpStatus.OK);
	}


	@GetMapping(value = "/findRecord/{id}")
	public ResponseEntity<Venta> getVentaById(@PathVariable Long id) throws ResourceNotFoundException {
		Venta venta = ventaServiceAPI.get(id);
		if (venta == null) {
			throw new ResourceNotFoundException("Record not found for <Venta> " + id);
		}
		return ResponseEntity.ok().body(venta);
	}

	// --------- MÉTODO UTILITARIO ---------
	private String getCorreoFromRequest(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (token != null && token.startsWith("Bearer ")) {
			token = token.replace("Bearer ", "");
			return jwtUtil.extractUsername(token);
		}
		return "desconocido";
	}

	// Puedes ponerlo como clase interna o archivo aparte
	public static class VentaRequest {
		public Venta venta;
		public Transaccion transaccion;
	}

}
