package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.Empresa;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.EmpresaServiceAPI;
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
@RequestMapping("/empresa")
public class EmpresaRestController {

    @Autowired
    private EmpresaServiceAPI empresaServiceAPI;

    @Autowired
    private AuditoriaServiceAPI auditoriaServiceAPI;

    @GetMapping("/getAll")
    public List<Empresa> getAll() {
        return empresaServiceAPI.getAll();
    }

    @PostMapping("/saveEmpresa")
    public ResponseEntity<Empresa> save(@RequestBody Empresa empresa, HttpServletRequest request) {
        String accionAuditoria = "I"; 

        if (empresa.getId() != null) {
            Empresa existente = empresaServiceAPI.get(empresa.getId());
            if (existente != null) {
                accionAuditoria = "U";  
            }
        }

        Empresa obj = empresaServiceAPI.save(empresa);

        Auditoria aud = new Auditoria();
        aud.setTablaAccion("empresa");
        aud.setAccionAudtria(accionAuditoria);
        aud.setUsrioAudtria("usuario"); // reemplazar por usuario real
        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria(
            (accionAuditoria.equals("I") ? "Creación" : "Actualización") + " de empresa con ID " + obj.getId()
        );
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));

        auditoriaServiceAPI.save(aud);

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @GetMapping("/findRecord/{id}")
    public ResponseEntity<Empresa> getById(@PathVariable Long id) throws ResourceNotFoundException {
        Empresa entidad = empresaServiceAPI.get(id);
        if (entidad == null) {
            throw new ResourceNotFoundException("Record not found for <Empresa> " + id);
        }
        return ResponseEntity.ok(entidad);
    }

   
}

