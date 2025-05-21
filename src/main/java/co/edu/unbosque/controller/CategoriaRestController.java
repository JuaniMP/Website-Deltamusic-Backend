package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.Categoria;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.CategoriaServiceAPI;
import co.edu.unbosque.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import co.edu.unbosque.utils.Util;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/categoria")
public class CategoriaRestController {

    @Autowired
    private CategoriaServiceAPI categoriaServiceAPI;
    
    @Autowired
    private AuditoriaServiceAPI auditoriaServiceAPI;

    @GetMapping("/getAll")
    public List<Categoria> getAll() {
        return categoriaServiceAPI.getAll();
    }

    @PostMapping("/saveCategoria")
    public ResponseEntity<Categoria> save(@RequestBody Categoria categoria, HttpServletRequest request) {
        String accionAuditoria = "I"; // por defecto insertar

        if (categoria.getId() != null) {
            Categoria existente = categoriaServiceAPI.get(categoria.getId());
            if (existente != null) {
                accionAuditoria = "U";  // actualizar
            }
        }

        Categoria obj = categoriaServiceAPI.save(categoria);

        Auditoria aud = new Auditoria();
        aud.setTablaAccion("categoria");
        aud.setAccionAudtria(accionAuditoria); 
        aud.setUsrioAudtria("usuario"); // aquí obtén el usuario real si puedes
        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria(
            (accionAuditoria.equals("I") ? "Creación" : "Actualización") + " de categoría con ID " + obj.getId()
        );
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));

        auditoriaServiceAPI.save(aud);

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }


    @GetMapping("/findRecord/{id}")
    public ResponseEntity<Categoria> getById(@PathVariable Long id) throws ResourceNotFoundException {
        Categoria entidad = categoriaServiceAPI.get(id);
        if (entidad == null) {
            throw new ResourceNotFoundException("Record not found for <Categoria> " + id);
        }
        return ResponseEntity.ok(entidad);
    }

    @DeleteMapping("/deleteCategoria/{id}")
    public ResponseEntity<Categoria> delete(@PathVariable Long id, HttpServletRequest request) {
        Categoria entidad = categoriaServiceAPI.get(id);
        if (entidad != null) {
            categoriaServiceAPI.delete(id);

            Auditoria aud = new Auditoria();
            aud.setTablaAccion("categoria");
            aud.setAccionAudtria("D"); 
            aud.setUsrioAudtria("usuario"); // Cambiar por usuario real
            aud.setIdTabla(id); 
            aud.setComentarioAudtria("Eliminación de categoría con ID " + id);
            aud.setFchaAudtria(new Date());
            aud.setAddressAudtria(Util.getClientIp(request));

            auditoriaServiceAPI.save(aud);

            return new ResponseEntity<>(entidad, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(entidad, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

