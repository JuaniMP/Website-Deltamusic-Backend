// src/main/java/co/edu/unbosque/controller/VentaRestController.java
package co.edu.unbosque.controller;

import co.edu.unbosque.entity.*;
import co.edu.unbosque.service.api.*;
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
import java.util.Optional;
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
    private ClienteServiceAPI clienteServiceAPI;

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

    @GetMapping("/getAll")
    public List<Venta> getAll() {
        return ventaServiceAPI.getAll();
    }

    @PostMapping("/saveVenta")
    public ResponseEntity<?> save(
            @RequestBody VentaRequest ventaRequest,
            HttpServletRequest request
    ) {
        // 0) Validar JSON mínimo
        if (ventaRequest == null
                || ventaRequest.venta == null
                || ventaRequest.transaccion == null
                || ventaRequest.venta.getDetalles() == null
                || ventaRequest.venta.getDetalles().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Debes enviar 'venta' (con lista de detalles) y 'transaccion'.");
        }

        // 1) Extraer correo del JWT
        String correoUsuario = getCorreoFromRequest(request);
        if (correoUsuario.equals("desconocido")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token inválido o no provisto en 'Authorization'.");
        }

        // 2) Buscar Cliente por correo
        Cliente cliente = clienteServiceAPI.findByCorreoCliente(correoUsuario);
        if (cliente == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No existe ningún cliente con correo: " + correoUsuario);
        }

     // 3) Construir objeto Venta con los campos que YA existen en la BD
        Venta venta = new Venta();
        // cliente.getId() es Long, pero setIdCliente espera Integer:
        venta.setIdCliente(cliente.getId().intValue());
        venta.setEstado((byte) 1);

        // 4) Fecha de venta
        if (ventaRequest.venta.getFechaVenta() != null) {
            venta.setFechaVenta(ventaRequest.venta.getFechaVenta());
        } else {
            venta.setFechaVenta(new Date());
        }

        // 5) Obtener parámetro IVA (solo uno)
        Optional<Parametro> optIva = parametroServiceAPI.findByDescripcionAndEstado("IVA", (byte) 1);
        if (optIva.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Parámetro IVA (estado=1) no encontrado.");
        }
        Parametro ivaParametro = optIva.get();
        int porcentajeIva = ivaParametro.getValorNumero();
        double ivaDecimal = porcentajeIva / 100.0;

        // 6) Validar máximo 3 productos comprados el mismo día
        int yaComprados = detalleVentaServiceAPI
                .totalProductosClientePorFecha(cliente.getId(), venta.getFechaVenta());

        // 7) Procesar cada detalle, validar stock y calcular totales
        int cantidadEnVenta = 0;
        java.math.BigDecimal sumatoriaBruta = java.math.BigDecimal.ZERO;
        int sumatoriaIva = 0;

        for (DetalleVenta detalle : ventaRequest.venta.getDetalles()) {
            // 7a) Verificar que exista el producto
            Producto producto = productoServiceAPI.get((long) detalle.getIdProducto());
            if (producto == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Producto ID " + detalle.getIdProducto() + " no existe.");
            }

            // 7b) Verificar stock
            if (producto.getExistencia() < detalle.getCantComp()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Stock insuficiente para: " + producto.getDescripcion()
                                + " (Quedan " + producto.getExistencia() + ")");
            }

            // 7c) Descontar y guardar producto
            int nuevaExist = producto.getExistencia() - detalle.getCantComp();
            producto.setExistencia(nuevaExist);
            productoServiceAPI.save(producto);

            // 7d) Calcular valorUnit e IVA del detalle
            int precioUnit = producto.getPrecioVentaActual().intValue();
            int subtotalLinea = precioUnit * detalle.getCantComp();
            int ivaLinea = 0;
            if (producto.getTieneIva() == 1) {
                ivaLinea = (int) Math.round(subtotalLinea * ivaDecimal);
            }
            detalle.setValorUnit(precioUnit);
            detalle.setValorIva(ivaLinea);

            detalle.setVenta(venta); // asocia este detalle con la venta

            sumatoriaBruta = sumatoriaBruta.add(
                    producto.getPrecioVentaActual()
                            .multiply(java.math.BigDecimal.valueOf(detalle.getCantComp()))
            );
            sumatoriaIva += ivaLinea;
            cantidadEnVenta += detalle.getCantComp();
        }

        // 8) Verificar que no supere 3 productos por día
        if ((yaComprados + cantidadEnVenta) > 3) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No puedes comprar más de 3 productos en el mismo día.");
        }

        // 9) Asignar totales a venta
        venta.setValorVenta(sumatoriaBruta.intValue());
        venta.setValorIva(sumatoriaIva);

        // 10) ¿Inserción o actualización?
        String accionAud = "I";
        if (venta.getId() != null) {
            Venta ex = ventaServiceAPI.get(venta.getId());
            if (ex != null) {
                accionAud = "U";
            }
        }

        // 11) Guardar la venta
        Venta ventaGuardada = ventaServiceAPI.save(venta);

        // --- 12) Procesar Transacción ---
        Transaccion tx = ventaRequest.transaccion;

        // 12a) Verificar método de pago
        MetodoPago mp = metodoPagoServiceAPI.get((long) tx.getIdMetodoPago());
        if (mp == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Método de pago ID " + tx.getIdMetodoPago() + " no válido.");
        }

        // 12b) Asignar a transacción: idCompra = venta.id, valorTx = venta.valorVenta + venta.valorIva, fechaHora = ahora
        tx.setIdCompra(ventaGuardada.getId().intValue());
        tx.setValorTx(ventaGuardada.getValorVenta() + ventaGuardada.getValorIva());
        tx.setFechaHora(new Date());

        // 12c) Validar idBanco (no puede ser null ni vacío)
        if (tx.getIdBanco() == null || tx.getIdBanco().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El campo 'idBanco' es obligatorio.");
        }

        // 12d) Asegurar valores "NA" en idFranquicia o numTarjeta si vienen nulos
        if (tx.getIdFranquicia() == null) {
            tx.setIdFranquicia("NA");
        }
        if (tx.getNumTarjeta() == null) {
            tx.setNumTarjeta("NA");
        }

        // 12e) Guardar Transacción
        Transaccion txGuardada = transaccionServiceAPI.save(tx);

        // --- 13) Guardar cada DetalleVenta en BD ---
        for (DetalleVenta dv : ventaRequest.venta.getDetalles()) {
            detalleVentaServiceAPI.save(dv);
        }

        // --- 14) Auditoría de la venta ---
        Auditoria aud = new Auditoria();
        aud.setTablaAccion("venta");
        aud.setAccionAudtria(accionAud);
        aud.setUsrioAudtria(correoUsuario);
        aud.setIdTabla(ventaGuardada.getId());
        aud.setComentarioAudtria(
                (accionAud.equals("I") ? "Creación" : "Actualización")
                        + " de venta con ID " + ventaGuardada.getId()
        );
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));
        auditoriaServiceAPI.save(aud);

        // --- 15) Envío de correo de resumen ---
     // 15) Envío de correo de resumen
        Optional<Usuario> optUsr = usuarioServiceAPI.findByCorreoUsuario(correoUsuario);
        if (optUsr.isPresent()) {
            Usuario usr = optUsr.get();
            String nombreCli = usr.getLoginUsrio();

            List<Producto> productosCorreo = ventaRequest.venta.getDetalles().stream()
                    .map(d -> productoServiceAPI.get((long) d.getIdProducto()))
                    .toList();

            emailService.enviarResumenCompra(
                    usr.getCorreoUsuario(),
                    nombreCli,
                    ventaGuardada,
                    ventaRequest.venta.getDetalles(),
                    productosCorreo,
                    txGuardada,
                    mp
            );
        }


        // 16) Retornar la venta guardada
        return new ResponseEntity<>(ventaGuardada, HttpStatus.OK);
    }

    @GetMapping("/findRecord/{id}")
    public ResponseEntity<Venta> getVentaById(@PathVariable Long id) throws ResourceNotFoundException {
        Venta v = ventaServiceAPI.get(id);
        if (v == null) {
            throw new ResourceNotFoundException("Record not found for <Venta> " + id);
        }
        return ResponseEntity.ok(v);
    }

    /**
     * Extrae el correo desde el token JWT en la cabecera "Authorization". 
     * Si no existe o no empieza con "Bearer ", retorna "desconocido".
     */
    private String getCorreoFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "");
            return jwtUtil.extractUsername(token);
        }
        return "desconocido";
    }

    /**
     * DTO para recibir:
     * {
     *   "venta": {
     *       "fechaVenta": "2025-06-02",   // opcional
     *       "detalles": [
     *           { "idProducto": 3, "cantComp": 1 },
     *           ...
     *       ]
     *   },
     *   "transaccion": {
     *       "idMetodoPago": 5,
     *       "idBanco": "Banco de Colombia",
     *       "idFranquicia": "NA",
     *       "numTarjeta": "NA"
     *   }
     * }
     */
    public static class VentaRequest {
        public Venta venta;
        public Transaccion transaccion;
    }
}


