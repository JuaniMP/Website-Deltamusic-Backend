package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.Cliente;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.ClienteServiceAPI;
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
@RequestMapping("/cliente")
public class ClienteRestController {

    @Autowired
    private ClienteServiceAPI clienteServiceAPI;

    @Autowired
    private AuditoriaServiceAPI auditoriaServiceAPI;

    @GetMapping("/getAll")
    public List<Cliente> getAll() {
        return clienteServiceAPI.getAll();
    }

    @PostMapping("/saveCliente")
    public ResponseEntity<Cliente> save(@RequestBody Cliente cliente, HttpServletRequest request) {
        String accionAuditoria = "I"; 

        if (cliente.getId() != null) {
            Cliente existente = clienteServiceAPI.get(cliente.getId());
            if (existente != null) {
                accionAuditoria = "U"; 
            }
        }

        Cliente obj = clienteServiceAPI.save(cliente);

        Auditoria aud = new Auditoria();
        aud.setTablaAccion("cliente");
        aud.setAccionAudtria(accionAuditoria);
        aud.setUsrioAudtria("usuario"); // Reemplaza con usuario real autenticado
        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria((accionAuditoria.equals("I") ? "Creación" : "Actualización") + " de cliente con ID " + obj.getId());
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));

        auditoriaServiceAPI.save(aud);

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @GetMapping("/findRecord/{id}")
    public ResponseEntity<Cliente> getById(@PathVariable Long id) throws ResourceNotFoundException {
        Cliente entidad = clienteServiceAPI.get(id);
        if (entidad == null) {
            throw new ResourceNotFoundException("Record not found for <Cliente> " + id);
        }
        return ResponseEntity.ok(entidad);
    }

  
}
