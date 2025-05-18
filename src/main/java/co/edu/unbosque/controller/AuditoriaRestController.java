package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaRestController {

    @Autowired
    private AuditoriaServiceAPI auditoriaServiceAPI;

    @GetMapping("/getAll")
    public List<Auditoria> getAll() {
        return auditoriaServiceAPI.getAll();
    }

    @PostMapping("/saveAuditoria")
    public ResponseEntity<Auditoria> save(
        @RequestParam String tablaAccion,
        @RequestParam String accionAudtria,
        @RequestParam String usuarioLogin,
        @RequestParam int idTabla,
        @RequestParam(required = false) String addressAudtria,
        @RequestParam(required = false) String comentarioAudtria
    ) {
        Auditoria aud = new Auditoria();
        aud.setTablaAccion(tablaAccion);
        aud.setAccionAudtria(accionAudtria);
        aud.setFchaAudtria(new Date());
        aud.setUsrioAudtria(usuarioLogin);
        aud.setIdTabla(idTabla);
        aud.setAddressAudtria(addressAudtria);
        aud.setComentarioAudtria(comentarioAudtria);

        Auditoria obj = auditoriaServiceAPI.save(aud);
        return new ResponseEntity<>(obj, HttpStatus.OK);
    }


    @GetMapping("/findRecord/{id}")
    public ResponseEntity<Auditoria> getById(@PathVariable Long id) throws ResourceNotFoundException {
        Auditoria entidad = auditoriaServiceAPI.get(id);
        if (entidad == null) {
            throw new ResourceNotFoundException("Record not found for <Auditoria> " + id);
        }
        return ResponseEntity.ok(entidad);
    }

    @DeleteMapping("/deleteAuditoria/{id}")
    public ResponseEntity<Auditoria> delete(@PathVariable Long id) {
        Auditoria entidad = auditoriaServiceAPI.get(id);
        if (entidad != null) {
            auditoriaServiceAPI.delete(id);
            return new ResponseEntity<>(entidad, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(entidad, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
