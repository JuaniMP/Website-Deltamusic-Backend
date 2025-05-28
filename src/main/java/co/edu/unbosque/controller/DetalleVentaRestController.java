package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.DetalleVenta;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.DetalleVentaServiceAPI;
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
@RequestMapping("/detalle_venta")
public class DetalleVentaRestController {

    @Autowired
    private DetalleVentaServiceAPI detalleVentaServiceAPI;

    @Autowired
    private AuditoriaServiceAPI auditoriaServiceAPI;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/getAll")
    public List<DetalleVenta> getAll() {
        return detalleVentaServiceAPI.getAll();
    }

    @PostMapping("/saveDetalleVenta")
    public ResponseEntity<DetalleVenta> save(@RequestBody DetalleVenta detalleVenta, HttpServletRequest request) {
        String accionAuditoria = "I";

        if (detalleVenta.getId() != null) {
            DetalleVenta existente = detalleVentaServiceAPI.get(detalleVenta.getId());
            if (existente != null) {
                accionAuditoria = "U";
            }
        }

        DetalleVenta obj = detalleVentaServiceAPI.save(detalleVenta);

        String correoUsuario = getCorreoFromRequest(request);

        Auditoria aud = new Auditoria();
        aud.setTablaAccion("detalle_venta");
        aud.setAccionAudtria(accionAuditoria);
        aud.setUsrioAudtria(correoUsuario);
        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria(
            (accionAuditoria.equals("I") ? "Creación" : "Actualización") + " de detalle venta con ID " + obj.getId()
        );
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));

        auditoriaServiceAPI.save(aud);

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @GetMapping("/findRecord/{id}")
    public ResponseEntity<DetalleVenta> getById(@PathVariable Long id) throws ResourceNotFoundException {
        DetalleVenta entidad = detalleVentaServiceAPI.get(id);
        if (entidad == null) {
            throw new ResourceNotFoundException("Record not found for <DetalleVenta> " + id);
        }
        return ResponseEntity.ok(entidad);
    }

    @DeleteMapping("/deleteDetalleVenta/{id}")
    public ResponseEntity<DetalleVenta> delete(@PathVariable Long id, HttpServletRequest request) {
        DetalleVenta entidad = detalleVentaServiceAPI.get(id);
        if (entidad != null) {
            detalleVentaServiceAPI.delete(id);

            String correoUsuario = getCorreoFromRequest(request);

            Auditoria aud = new Auditoria();
            aud.setTablaAccion("detalle_venta");
            aud.setAccionAudtria("D");
            aud.setUsrioAudtria(correoUsuario);
            aud.setIdTabla(id);
            aud.setComentarioAudtria("Eliminación de detalle venta con ID " + id);
            aud.setFchaAudtria(new Date());
            aud.setAddressAudtria(Util.getClientIp(request));

            auditoriaServiceAPI.save(aud);

            return new ResponseEntity<>(entidad, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(entidad, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
