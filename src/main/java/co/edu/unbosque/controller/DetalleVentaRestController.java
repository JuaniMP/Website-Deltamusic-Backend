package co.edu.unbosque.controller;

import co.edu.unbosque.entity.DetalleVenta;
import co.edu.unbosque.service.api.DetalleVentaServiceAPI;
import co.edu.unbosque.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/detalle_venta")
public class DetalleVentaRestController {

    @Autowired
    private DetalleVentaServiceAPI detalleVentaServiceAPI;

    @GetMapping("/getAll")
    public List<DetalleVenta> getAll() {
        return detalleVentaServiceAPI.getAll();
    }

    @PostMapping("/saveDetalleVenta")
    public ResponseEntity<DetalleVenta> save(@RequestBody DetalleVenta detalleVenta) {
        DetalleVenta obj = detalleVentaServiceAPI.save(detalleVenta);
        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @GetMapping("/findRecord/{id}")
    public ResponseEntity<DetalleVenta> getById(@PathVariable Long id) throws ResourceNotFoundException {
        DetalleVenta entidad = detalleVentaServiceAPI.get(id);
        if (entidad == null) {
            throw new ResourceNotFoundException("Record not found for <DetalleVenta> " + id);
        }
        return ResponseEntity.ok(entidad);
    }

    @DeleteMapping("/deleteDetalleVenta/{id}")
    public ResponseEntity<DetalleVenta> delete(@PathVariable Long id) {
        DetalleVenta entidad = detalleVentaServiceAPI.get(id);
        if (entidad != null) {
            detalleVentaServiceAPI.delete(id);
            return new ResponseEntity<>(entidad, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(entidad, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
