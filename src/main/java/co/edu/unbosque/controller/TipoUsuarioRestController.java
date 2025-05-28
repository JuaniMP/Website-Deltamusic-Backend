package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.TipoUsuario;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.TipoUsuarioServiceAPI;
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
@RequestMapping("/tipousuario")
public class TipoUsuarioRestController {

    @Autowired
    private TipoUsuarioServiceAPI tipoUsuarioServiceAPI;

    @Autowired
    private AuditoriaServiceAPI auditoriaServiceAPI;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping(value = "/getAll")
    public List<TipoUsuario> getAll() {
        return tipoUsuarioServiceAPI.getAll();
    }

    @PostMapping(value = "/saveTipoUsuario")
    public ResponseEntity<TipoUsuario> save(@RequestBody TipoUsuario tipoUsuario, HttpServletRequest request) {
        String accionAuditoria = "I"; // Por defecto, inserción

        if (tipoUsuario.getId() != null) {
            TipoUsuario existente = tipoUsuarioServiceAPI.get(tipoUsuario.getId());
            if (existente != null) {
                accionAuditoria = "U"; // Actualización
            }
        }

        TipoUsuario obj = tipoUsuarioServiceAPI.save(tipoUsuario);

        String correoUsuario = getCorreoFromRequest(request);

        Auditoria aud = new Auditoria();
        aud.setTablaAccion("tipousuario");
        aud.setAccionAudtria(accionAuditoria);
        aud.setUsrioAudtria(correoUsuario); // correo autenticado real
        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria(
                (accionAuditoria.equals("I") ? "Creación" : "Actualización") + " de tipo usuario con ID " + obj.getId()
        );
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));

        auditoriaServiceAPI.save(aud);

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @GetMapping(value = "/findRecord/{id}")
    public ResponseEntity<TipoUsuario> getTipoUsuarioById(@PathVariable Long id) throws ResourceNotFoundException {
        TipoUsuario tipoUsuario = tipoUsuarioServiceAPI.get(id);
        if (tipoUsuario == null) {
            throw new ResourceNotFoundException("Record not found for <TipoUsuario> " + id);
        }
        return ResponseEntity.ok(tipoUsuario);
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
