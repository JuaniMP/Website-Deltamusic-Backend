package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.TipoUsuario;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.TipoUsuarioServiceAPI;
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
@RequestMapping("/tipousuario")
public class TipoUsuarioRestController {

    @Autowired
    private TipoUsuarioServiceAPI tipoUsuarioServiceAPI;

    @Autowired
    private AuditoriaServiceAPI auditoriaServiceAPI;

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

        Auditoria aud = new Auditoria();
        aud.setTablaAccion("tipousuario");
        aud.setAccionAudtria(accionAuditoria);
        aud.setUsrioAudtria("usuario"); // Cambiar por usuario autenticado real
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


}
