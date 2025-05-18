package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Cliente;
import co.edu.unbosque.service.api.ClienteServiceAPI;
import co.edu.unbosque.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cliente")
public class ClienteRestController {

    @Autowired
    private ClienteServiceAPI clienteServiceAPI;

    @GetMapping("/getAll")
    public List<Cliente> getAll() {
        return clienteServiceAPI.getAll();
    }

    @PostMapping("/saveCliente")
    public ResponseEntity<Cliente> save(@RequestBody Cliente cliente) {
        Cliente obj = clienteServiceAPI.save(cliente);
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

    @DeleteMapping("/deleteCliente/{id}")
    public ResponseEntity<Cliente> delete(@PathVariable Long id) {
        Cliente entidad = clienteServiceAPI.get(id);
        if (entidad != null) {
            clienteServiceAPI.delete(id);
            return new ResponseEntity<>(entidad, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(entidad, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
