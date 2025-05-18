package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Empresa;
import co.edu.unbosque.service.api.EmpresaServiceAPI;
import co.edu.unbosque.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/empresa")
public class EmpresaRestController {

    @Autowired
    private EmpresaServiceAPI empresaServiceAPI;

    @GetMapping("/getAll")
    public List<Empresa> getAll() {
        return empresaServiceAPI.getAll();
    }

    @PostMapping("/saveEmpresa")
    public ResponseEntity<Empresa> save(@RequestBody Empresa empresa) {
        Empresa obj = empresaServiceAPI.save(empresa);
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

    @DeleteMapping("/deleteEmpresa/{id}")
    public ResponseEntity<Empresa> delete(@PathVariable Long id) {
        Empresa entidad = empresaServiceAPI.get(id);
        if (entidad != null) {
            empresaServiceAPI.delete(id);
            return new ResponseEntity<>(entidad, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(entidad, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
