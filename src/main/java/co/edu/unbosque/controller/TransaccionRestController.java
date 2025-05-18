package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Transaccion; 
import co.edu.unbosque.service.api.TransaccionServiceAPI; 
import co.edu.unbosque.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin(origins = "http://localhost:8181",maxAge = 3600)
@RestController
@RequestMapping("/transaccion") 
public class TransaccionRestController { 

    @Autowired
    private TransaccionServiceAPI transaccionServiceAPI; 

    @GetMapping(value="/getAll")
    //ResponseEntity List<Transaccion> getAll(){
    public List<Transaccion> getAll(){
        return transaccionServiceAPI.getAll(); 
    }

    @PostMapping(value="/saveTransaccion")
    public ResponseEntity<Transaccion> save(@RequestBody Transaccion transaccion){ 
        Transaccion obj = transaccionServiceAPI.save(transaccion); 
        return new ResponseEntity<Transaccion>(obj, HttpStatus.OK); // 200
    }

    @GetMapping(value="/findRecord/{id}")
    public ResponseEntity<Transaccion> getTransaccionById(@PathVariable Long id) 
            throws ResourceNotFoundException {
        Transaccion transaccion = transaccionServiceAPI.get(id); 
        if (transaccion == null){
            new ResourceNotFoundException("Record not found for <Transaccion>"+id); 
        }
        return ResponseEntity.ok().body(transaccion); 
    }

    @DeleteMapping(value="/deleteTransaccion/{id}") 
    public ResponseEntity<Transaccion> delete(@PathVariable Long id){
        Transaccion transaccion = transaccionServiceAPI.get(id); 
        if (transaccion != null){
            transaccionServiceAPI.delete(id); 
        }else{
            return new ResponseEntity<Transaccion>(transaccion, HttpStatus.INTERNAL_SERVER_ERROR); 
        }
        return new ResponseEntity<Transaccion>(transaccion, HttpStatus.OK); 
    }
}
