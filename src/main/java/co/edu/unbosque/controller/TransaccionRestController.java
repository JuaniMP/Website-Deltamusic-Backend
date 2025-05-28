package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.Transaccion;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.TransaccionServiceAPI;
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
@RequestMapping("/transaccion")
public class TransaccionRestController {

    @Autowired
    private TransaccionServiceAPI transaccionServiceAPI;

    @Autowired
    private AuditoriaServiceAPI auditoriaServiceAPI;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping(value = "/getAll")
    public List<Transaccion> getAll() {
        return transaccionServiceAPI.getAll();
    }

    @PostMapping(value = "/saveTransaccion")
    public ResponseEntity<Transaccion> save(@RequestBody Transaccion transaccion, HttpServletRequest request) {
        String accionAuditoria = "I"; // Por defecto inserción

        if (transaccion.getId() != null) {
            Transaccion existente = transaccionServiceAPI.get(transaccion.getId());
            if (existente != null) {
                accionAuditoria = "U"; // Actualización
            }
        }

        Transaccion obj = transaccionServiceAPI.save(transaccion);

        String correoUsuario = getCorreoFromRequest(request);

        Auditoria aud = new Auditoria();
        aud.setTablaAccion("transaccion");
        aud.setAccionAudtria(accionAuditoria);
        aud.setUsrioAudtria(correoUsuario); // Correo autenticado real
        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria(
            (accionAuditoria.equals("I") ? "Creación" : "Actualización") + " de transacción con ID " + obj.getId()
        );
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));

        auditoriaServiceAPI.save(aud);

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @GetMapping(value = "/findRecord/{id}")
    public ResponseEntity<Transaccion> getTransaccionById(@PathVariable Long id) throws ResourceNotFoundException {
        Transaccion transaccion = transaccionServiceAPI.get(id);
        if (transaccion == null) {
            throw new ResourceNotFoundException("Record not found for <Transaccion> " + id);
        }
        return ResponseEntity.ok().body(transaccion);
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
