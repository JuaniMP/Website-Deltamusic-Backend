package co.edu.unbosque.controller;

import co.edu.unbosque.entity.MetodoPago;
import co.edu.unbosque.service.api.MetodoPagoServiceAPI;
import co.edu.unbosque.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/metodo_pago")
public class MetodoPagoRestController {

    @Autowired
    private MetodoPagoServiceAPI metodoPagoServiceAPI;

    @GetMapping("/getAll")
    public List<MetodoPago> getAll() {
        return metodoPagoServiceAPI.getAll();
    }

    @PostMapping("/saveMetodoPago")
    public ResponseEntity<MetodoPago> save(@RequestBody MetodoPago metodoPago) {
        MetodoPago obj = metodoPagoServiceAPI.save(metodoPago);
        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @GetMapping("/findRecord/{id}")
    public ResponseEntity<MetodoPago> getById(@PathVariable Long id) throws ResourceNotFoundException {
        MetodoPago entidad = metodoPagoServiceAPI.get(id);
        if (entidad == null) {
            throw new ResourceNotFoundException("Record not found for <MetodoPago> " + id);
        }
        return ResponseEntity.ok(entidad);
    }

    @DeleteMapping("/deleteMetodoPago/{id}")
    public ResponseEntity<MetodoPago> delete(@PathVariable Long id) {
        MetodoPago entidad = metodoPagoServiceAPI.get(id);
        if (entidad != null) {
            metodoPagoServiceAPI.delete(id);
            return new ResponseEntity<>(entidad, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(entidad, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
