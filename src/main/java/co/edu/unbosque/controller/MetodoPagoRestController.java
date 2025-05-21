package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.MetodoPago;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.MetodoPagoServiceAPI;
import co.edu.unbosque.utils.ResourceNotFoundException;
import co.edu.unbosque.utils.Util;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/metodo_pago")
public class MetodoPagoRestController {

    @Autowired
    private MetodoPagoServiceAPI metodoPagoServiceAPI;

    @Autowired
    private AuditoriaServiceAPI auditoriaServiceAPI;

    @GetMapping("/getAll")
    public List<MetodoPago> getAll() {
        return metodoPagoServiceAPI.getAll();
    }

    @PostMapping("/saveMetodoPago")
    public ResponseEntity<MetodoPago> save(@RequestBody MetodoPago metodoPago, HttpServletRequest request) {
        String accionAuditoria = "I"; // Por defecto inserción

        if (metodoPago.getId() != null) {
            MetodoPago existente = metodoPagoServiceAPI.get(metodoPago.getId());
            if (existente != null) {
                accionAuditoria = "U"; // Actualización
            }
        }

        MetodoPago obj = metodoPagoServiceAPI.save(metodoPago);

        Auditoria aud = new Auditoria();
        aud.setTablaAccion("metodo_pago");
        aud.setAccionAudtria(accionAuditoria);
        aud.setUsrioAudtria("usuario"); // Cambiar por usuario autenticado real
        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria(
            (accionAuditoria.equals("I") ? "Creación" : "Actualización") + " de método de pago con ID " + obj.getId()
        );
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));

        auditoriaServiceAPI.save(aud);

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @GetMapping("/findRecord/{id}")
    public ResponseEntity<MetodoPago> getById(@PathVariable Long id) throws ResourceNotFoundException {
        MetodoPago entidad = metodoPagoServiceAPI.get(id);
        if (entidad == null) {
            throw new ResourceNotFoundException("Record not found for <MetodoPago> " + id);
        }
        return ResponseEntity.ok(entidad);
    }

    @DeleteMapping("/deleteMetodoPago/{id}")
    public ResponseEntity<MetodoPago> delete(@PathVariable Long id, HttpServletRequest request) {
        MetodoPago entidad = metodoPagoServiceAPI.get(id);
        if (entidad != null) {
            metodoPagoServiceAPI.delete(id);

            Auditoria aud = new Auditoria();
            aud.setTablaAccion("metodo_pago");
            aud.setAccionAudtria("D");
            aud.setUsrioAudtria("usuario"); // Cambiar por usuario autenticado real
            aud.setIdTabla(id);
            aud.setComentarioAudtria("Eliminación de método de pago con ID " + id);
            aud.setFchaAudtria(new Date());
            aud.setAddressAudtria(Util.getClientIp(request));

            auditoriaServiceAPI.save(aud);

            return new ResponseEntity<>(entidad, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(entidad, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
