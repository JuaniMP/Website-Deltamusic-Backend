package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Categoria;
import co.edu.unbosque.service.api.CategoriaServiceAPI;
import co.edu.unbosque.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categoria")
public class CategoriaRestController {

    @Autowired
    private CategoriaServiceAPI categoriaServiceAPI;

    @GetMapping("/getAll")
    public List<Categoria> getAll() {
        return categoriaServiceAPI.getAll();
    }

    @PostMapping("/saveCategoria")
    public ResponseEntity<Categoria> save(@RequestBody Categoria categoria) {
        Categoria obj = categoriaServiceAPI.save(categoria);
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
    public ResponseEntity<Categoria> delete(@PathVariable Long id) {
        Categoria entidad = categoriaServiceAPI.get(id);
        if (entidad != null) {
            categoriaServiceAPI.delete(id);
            return new ResponseEntity<>(entidad, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(entidad, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
