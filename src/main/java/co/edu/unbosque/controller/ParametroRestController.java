package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.Parametro;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.ParametroServiceAPI;
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
@RequestMapping("/parametro")
public class ParametroRestController {

    @Autowired
    private ParametroServiceAPI parametroServiceAPI;

    @Autowired
    private AuditoriaServiceAPI auditoriaServiceAPI;

    @GetMapping(value = "/getAll")
    public List<Parametro> getAll() {
        return parametroServiceAPI.getAll();
    }

    @PostMapping(value = "/saveParametro")
    public ResponseEntity<Parametro> save(@RequestBody Parametro parametro, HttpServletRequest request) {
        String accionAuditoria = "I"; // por defecto insertar

        if (parametro.getId() != null) {
            Parametro existente = parametroServiceAPI.get(parametro.getId());
            if (existente != null) {
                accionAuditoria = "U"; 
            }
        }

        Parametro obj = parametroServiceAPI.save(parametro);

        Auditoria aud = new Auditoria();
        aud.setTablaAccion("parametro");
        aud.setAccionAudtria(accionAuditoria);
        aud.setUsrioAudtria("usuario"); // Cambiar por usuario real autenticado
        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria(
                (accionAuditoria.equals("I") ? "Creación" : "Actualización") + " de parámetro con ID " + obj.getId()
        );
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));

        auditoriaServiceAPI.save(aud);

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @GetMapping(value = "/findRecord/{id}")
    public ResponseEntity<Parametro> getParametroById(@PathVariable Long id) throws ResourceNotFoundException {
        Parametro parametro = parametroServiceAPI.get(id);
        if (parametro == null) {
            throw new ResourceNotFoundException("Record not found for <Parametro> " + id);
        }
        return ResponseEntity.ok().body(parametro);
    }

    @DeleteMapping(value = "/deleteParametro/{id}")
    public ResponseEntity<Parametro> delete(@PathVariable Long id, HttpServletRequest request) {
        Parametro parametro = parametroServiceAPI.get(id);
        if (parametro != null) {
            parametroServiceAPI.delete(id);

            Auditoria aud = new Auditoria();
            aud.setTablaAccion("parametro");
            aud.setAccionAudtria("D");
            aud.setUsrioAudtria("usuario"); // Cambiar por usuario real autenticado
            aud.setIdTabla(id);
            aud.setComentarioAudtria("Eliminación de parámetro con ID " + id);
            aud.setFchaAudtria(new Date());
            aud.setAddressAudtria(Util.getClientIp(request));

            auditoriaServiceAPI.save(aud);

            return new ResponseEntity<>(parametro, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(parametro, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
