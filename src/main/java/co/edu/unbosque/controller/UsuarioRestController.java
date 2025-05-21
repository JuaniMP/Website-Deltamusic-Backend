package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.Usuario;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.UsuarioServiceAPI;
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
@RequestMapping("/usuario")
public class UsuarioRestController {

    @Autowired
    private UsuarioServiceAPI usuarioServiceAPI;

    @Autowired
    private AuditoriaServiceAPI auditoriaServiceAPI;

    @Autowired
    private Util util;

    @GetMapping(value = "/getAll")
    public List<Usuario> getAll() {
        return usuarioServiceAPI.getAll();
    }

    @PostMapping(value = "/saveUsuario")
    public ResponseEntity<Usuario> save(@RequestBody Usuario usuario, HttpServletRequest request) {
        usuario.setClaveUsrio(util.generarHash(usuario, usuario.getClaveUsrio()));

        String accionAuditoria = "I"; // Por defecto inserción

        if (usuario.getId() != null) {
            Usuario existente = usuarioServiceAPI.get(usuario.getId());
            if (existente != null) {
                accionAuditoria = "U"; // Actualización
            }
        }

        Usuario obj = usuarioServiceAPI.save(usuario);

        Auditoria aud = new Auditoria();
        aud.setTablaAccion("usuario");
        aud.setAccionAudtria(accionAuditoria);
        aud.setUsrioAudtria("usuario"); // Cambiar por usuario autenticado real
        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria(
                (accionAuditoria.equals("I") ? "Creación" : "Actualización") + " de usuario con ID " + obj.getId()
        );
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));

        auditoriaServiceAPI.save(aud);

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @GetMapping(value = "/findRecord/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) throws ResourceNotFoundException {
        Usuario usuario = usuarioServiceAPI.get(id);
        if (usuario == null) {
            throw new ResourceNotFoundException("Record not found for <Usuario> " + id);
        }
        return ResponseEntity.ok(usuario);
    }

}
