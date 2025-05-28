package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.Venta;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.VentaServiceAPI;
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
    private AuditoriaServiceAPI auditoriaServiceAPI;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping(value = "/getAll")
    public List<Venta> getAll() {
        return ventaServiceAPI.getAll();
    }

    @PostMapping(value = "/saveVenta")
    public ResponseEntity<Venta> save(@RequestBody Venta venta, HttpServletRequest request) {
        String accionAuditoria = "I"; // Por defecto inserción

        if (venta.getId() != null) {
            Venta existente = ventaServiceAPI.get(venta.getId());
            if (existente != null) {
                accionAuditoria = "U"; // Actualización
            }
        }

        Venta obj = ventaServiceAPI.save(venta);

        String correoUsuario = getCorreoFromRequest(request);

        Auditoria aud = new Auditoria();
        aud.setTablaAccion("venta");
        aud.setAccionAudtria(accionAuditoria);
        aud.setUsrioAudtria(correoUsuario); // Correo autenticado real
        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria(
            (accionAuditoria.equals("I") ? "Creación" : "Actualización") + " de venta con ID " + obj.getId()
        );
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));

        auditoriaServiceAPI.save(aud);

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
}
