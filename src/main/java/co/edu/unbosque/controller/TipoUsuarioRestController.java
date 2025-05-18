package co.edu.unbosque.controller;

import co.edu.unbosque.entity.TipoUsuario; 
import co.edu.unbosque.service.api.TipoUsuarioServiceAPI; 
import co.edu.unbosque.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin(origins = "http://localhost:8181",maxAge = 3600)
@RestController
@RequestMapping("/tipousuario") 
public class TipoUsuarioRestController { 

    @Autowired
    private TipoUsuarioServiceAPI tipoUsuarioServiceAPI; 

    @GetMapping(value="/getAll")
    //ResponseEntity List<TipoUsuario> getAll(){
    public List<TipoUsuario> getAll(){
        return tipoUsuarioServiceAPI.getAll(); 
    }

    @PostMapping(value="/saveTipoUsuario") // Cambié /saveUsuario por /saveTipoUsuario
    public ResponseEntity<TipoUsuario> save(@RequestBody TipoUsuario tipoUsuario){ 
        TipoUsuario obj = tipoUsuarioServiceAPI.save(tipoUsuario); 
        return new ResponseEntity<TipoUsuario>(obj, HttpStatus.OK); // 200
    }

    @GetMapping(value="/findRecord/{id}")
    public ResponseEntity<TipoUsuario> getTipoUsuarioById(@PathVariable Long id) 
            throws ResourceNotFoundException {
        TipoUsuario tipoUsuario = tipoUsuarioServiceAPI.get(id); 
        if (tipoUsuario == null){
            new ResourceNotFoundException("Record not found for <TipoUsuario>"+id); 
        }
        return ResponseEntity.ok().body(tipoUsuario); 
    }

    @DeleteMapping(value="/deleteTipoUsuario/{id}") 
    public ResponseEntity<TipoUsuario> delete(@PathVariable Long id){
        TipoUsuario tipoUsuario = tipoUsuarioServiceAPI.get(id); 
        if (tipoUsuario != null){
            tipoUsuarioServiceAPI.delete(id); 
        }else{
            return new ResponseEntity<TipoUsuario>(tipoUsuario, HttpStatus.INTERNAL_SERVER_ERROR); 
        }
        return new ResponseEntity<TipoUsuario>(tipoUsuario, HttpStatus.OK); 
    }
}
