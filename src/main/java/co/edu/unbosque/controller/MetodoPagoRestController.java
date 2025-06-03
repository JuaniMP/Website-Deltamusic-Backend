package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.MetodoPago;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.MetodoPagoServiceAPI;
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
@RequestMapping("/metodo_pago")
public class MetodoPagoRestController {

    @Autowired
    private MetodoPagoServiceAPI metodoPagoServiceAPI;

    @Autowired
    private AuditoriaServiceAPI auditoriaServiceAPI;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/getAll")
    public List<MetodoPago> getAll() {
        return metodoPagoServiceAPI.getAll();
    }

    @PostMapping("/saveMetodoPago")
    public ResponseEntity<MetodoPago> save(@RequestBody MetodoPago metodoPago, HttpServletRequest request) {
        String accionAuditoria = "I";

        if (metodoPago.getId() != null) {
            MetodoPago existente = metodoPagoServiceAPI.get(metodoPago.getId());
            if (existente != null) {
                accionAuditoria = "U";
            }
        }

        MetodoPago obj = metodoPagoServiceAPI.save(metodoPago);

        String correoUsuario = getCorreoFromRequest(request);

        Auditoria aud = new Auditoria();
        aud.setTablaAccion("metodo_pago");
        aud.setAccionAudtria(accionAuditoria);
        aud.setUsrioAudtria(correoUsuario);
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

            String correoUsuario = getCorreoFromRequest(request);

            Auditoria aud = new Auditoria();
            aud.setTablaAccion("metodo_pago");
            aud.setAccionAudtria("D");
            aud.setUsrioAudtria(correoUsuario);
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

    private String getCorreoFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "");
            return jwtUtil.extractUsername(token);
        }
        return "desconocido";
    }
}
