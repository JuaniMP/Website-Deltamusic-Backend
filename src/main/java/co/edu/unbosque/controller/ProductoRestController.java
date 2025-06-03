package co.edu.unbosque.controller;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.Producto;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.ProductoServiceAPI;
import co.edu.unbosque.utils.JwtUtil;
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

    @GetMapping("/getAll")
    public List<Producto> getAll() {
        return productoServiceAPI.getAll();
    }

    /**
     * Se usa tanto para CREAR como para ACTUALIZAR producto.
     * Si producto.getId() != null y existe, se considera "U". Si no, "I".
     */
    @PostMapping("/saveProducto")
    public ResponseEntity<?> save(@RequestBody Producto producto, HttpServletRequest request) {
        String accionAuditoria = "I";

        // Validar existencia y stock máximo
        if (producto.getExistencia() < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("La existencia no puede ser negativa.");
        }
        if (producto.getStockMaximo() < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("El stock máximo no puede ser negativo.");
        }
        if (producto.getExistencia() > producto.getStockMaximo()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("La existencia no puede ser mayor al stock máximo.");
        }

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
        aud.setUsrioAudtria(correoUsuario);
        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria(
                (accionAuditoria.equals("I") ? "Creación" : "Actualización") + " de producto con ID " + obj.getId()
        );
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));

        auditoriaServiceAPI.save(aud);

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @DeleteMapping("/deleteProducto/{id}")
    public ResponseEntity<Producto> delete(@PathVariable Long id, HttpServletRequest request) {
        Producto producto = productoServiceAPI.get(id);
        if (producto != null) {
            productoServiceAPI.delete(id);

            String correoUsuario = getCorreoFromRequest(request);

            Auditoria aud = new Auditoria();
            aud.setTablaAccion("producto");
            aud.setAccionAudtria("D");
            aud.setUsrioAudtria(correoUsuario);
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

    private String getCorreoFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "");
            return jwtUtil.extractUsername(token);
        }
        return "desconocido";
    }
}
