package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.Producto;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.ProductoServiceAPI;
import co.edu.unbosque.utils.JwtUtil;
import co.edu.unbosque.utils.ResourceNotFoundException;
import co.edu.unbosque.utils.Util;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("/producto")
public class ProductoRestController {

    @Autowired
    private ProductoServiceAPI productoServiceAPI;

    @Autowired
    private AuditoriaServiceAPI auditoriaServiceAPI;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping(value = "/getAll")
    public List<Producto> getAll() {
        return productoServiceAPI.getAll();
    }

    @PostMapping(value = "/saveProducto")
    public ResponseEntity<Producto> save(@RequestBody Producto producto, HttpServletRequest request) {
        String accionAuditoria = "I"; // Por defecto inserción

        if (producto.getId() != null) {
            Producto existente = productoServiceAPI.get(producto.getId());
            if (existente != null) {
                accionAuditoria = "U"; 
            }
        }

        Producto obj = productoServiceAPI.save(producto);

        String correoUsuario = getCorreoFromRequest(request);

        Auditoria aud = new Auditoria();
        aud.setTablaAccion("producto");
        aud.setAccionAudtria(accionAuditoria);
        aud.setUsrioAudtria(correoUsuario); // correo autenticado real
        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria(
                (accionAuditoria.equals("I") ? "Creación" : "Actualización") + " de producto con ID " + obj.getId()
        );
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));

        auditoriaServiceAPI.save(aud);

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @GetMapping(value = "/findRecord/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable Long id) throws ResourceNotFoundException {
        Producto producto = productoServiceAPI.get(id);
        if (producto == null) {
            throw new ResourceNotFoundException("Record not found for <Producto> " + id);
        }
        return ResponseEntity.ok().body(producto);
    }

    @DeleteMapping(value = "/deleteProducto/{id}")
    public ResponseEntity<Producto> delete(@PathVariable Long id, HttpServletRequest request) {
        Producto producto = productoServiceAPI.get(id);
        if (producto != null) {
            productoServiceAPI.delete(id);

            String correoUsuario = getCorreoFromRequest(request);

            Auditoria aud = new Auditoria();
            aud.setTablaAccion("producto");
            aud.setAccionAudtria("D");
            aud.setUsrioAudtria(correoUsuario); // correo autenticado real
            aud.setIdTabla(id);
            aud.setComentarioAudtria("Eliminación de producto con ID " + id);
            aud.setFchaAudtria(new Date());
            aud.setAddressAudtria(Util.getClientIp(request));

            auditoriaServiceAPI.save(aud);

            return new ResponseEntity<>(producto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(producto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --------- MÉTODO UTILITARIO ---------
    private String getCorreoFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "");
            return jwtUtil.extractUsername(token);
        }
        return "desconocido";
    }
}
