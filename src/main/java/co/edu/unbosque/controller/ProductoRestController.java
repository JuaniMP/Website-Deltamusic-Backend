package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Producto; 
import co.edu.unbosque.service.api.ProductoServiceAPI; 
import co.edu.unbosque.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin(origins = "http://localhost:8181",maxAge = 3600)
@RestController
@RequestMapping("/producto") 
public class ProductoRestController { 

    @Autowired
    private ProductoServiceAPI productoServiceAPI; 

    @GetMapping(value="/getAll")
    //ResponseEntity List<Producto> getAll(){
    public List<Producto> getAll(){
        return productoServiceAPI.getAll(); 
    }

    @PostMapping(value="/saveProducto") // Cambié /saveUsuario por /saveProducto
    public ResponseEntity<Producto> save(@RequestBody Producto producto){ 
        Producto obj = productoServiceAPI.save(producto); 
        return new ResponseEntity<Producto>(obj, HttpStatus.OK); // 200
    }

    @GetMapping(value="/findRecord/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable Long id) 
            throws ResourceNotFoundException {
        Producto producto = productoServiceAPI.get(id); 
        if (producto == null){
            new ResourceNotFoundException("Record not found for <Producto>"+id); 
        }
        return ResponseEntity.ok().body(producto); 
    }

    @DeleteMapping(value="/deleteProducto/{id}") 
    public ResponseEntity<Producto> delete(@PathVariable Long id){
        Producto producto = productoServiceAPI.get(id); 
        if (producto != null){
            productoServiceAPI.delete(id); 
        }else{
            return new ResponseEntity<Producto>(producto, HttpStatus.INTERNAL_SERVER_ERROR); 
        }
        return new ResponseEntity<Producto>(producto, HttpStatus.OK); 
    }
}
