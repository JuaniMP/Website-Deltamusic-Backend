package co.edu.unbosque.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.entity.Auditoria;
import co.edu.unbosque.entity.Usuario;
import co.edu.unbosque.service.api.AuditoriaServiceAPI;
import co.edu.unbosque.service.api.UsuarioServiceAPI;
import co.edu.unbosque.utils.JwtUtil;
import co.edu.unbosque.utils.LoginRequest;
import co.edu.unbosque.utils.ResourceNotFoundException;
import co.edu.unbosque.utils.Util;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("/usuario")
public class UsuarioRestController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioServiceAPI usuarioServiceAPI;

    @Autowired
    private AuditoriaServiceAPI auditoriaServiceAPI;

    @Autowired
    private Util utilidad;

    @GetMapping("/getAll")
    public List<Usuario> getAll() {
        return usuarioServiceAPI.getAll();
    }

    @PostMapping("/saveUsuario")
    public ResponseEntity<Usuario> save(@RequestBody Usuario usuario, HttpServletRequest request) {
        // Normalizar login
        String loginNormalizado = usuario.getLoginUsrio().toLowerCase().trim();
        usuario.setLoginUsrio(loginNormalizado);

        // Generar hash de la clave
        usuario.setClaveUsrio(utilidad.generarHash(usuario, usuario.getClaveUsrio()));

        // Fecha de última clave
        if (usuario.getFchaUltmaClave() == null) {
            usuario.setFchaUltmaClave(new Date());
        }

        // Determinar acción de auditoría
        String accionAuditoria = "I";
        if (usuario.getId() != null && usuarioServiceAPI.get(usuario.getId()) != null) {
            accionAuditoria = "U";
        }

        Usuario obj = usuarioServiceAPI.save(usuario);

        // Crear auditoría
        Auditoria aud = new Auditoria();
        aud.setTablaAccion("usuario");
        aud.setAccionAudtria(accionAuditoria);

        // Extraer usuario autenticado del token
        String usernameAud = "anonymous";
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            usernameAud = jwtUtil.getLoginFromToken(token);
        }
        aud.setUsrioAudtria(usernameAud);

        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria((accionAuditoria.equals("I") ? "Creación" : "Actualización")
                + " de usuario con ID " + obj.getId());
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));
        auditoriaServiceAPI.save(aud);

        return new ResponseEntity<>(obj, HttpStatus.OK);
    }

    @GetMapping("/findRecord/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) throws ResourceNotFoundException {
        Usuario usuario = usuarioServiceAPI.get(id);
        if (usuario == null) {
            throw new ResourceNotFoundException("Record not found for <Usuario> " + id);
        }
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String correoNormalizado = loginRequest.getCorreoUsuario().toLowerCase().trim();

        Optional<Usuario> userOpt = usuarioServiceAPI.findByCorreoUsuario(correoNormalizado);
        if (userOpt.isEmpty()) {
            return new ResponseEntity<>("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }
        Usuario user = userOpt.get();

        String claveUsuario = utilidad.generarHash(user, loginRequest.getClave());

        // Si la contraseña es incorrecta, incrementamos intentos y guardamos
        if (!user.getClaveUsrio().equals(claveUsuario)) {
            user.setIntentos(user.getIntentos() + 1);
            usuarioServiceAPI.save(user);
            return new ResponseEntity<>("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }

        // Resetear contador de intentos tras login exitoso
        user.setIntentos(0);
        usuarioServiceAPI.save(user);

        String token = jwtUtil.generateToken(user.getLoginUsrio());

        Map<String, Object> response = new HashMap<>();
        response.put("usuario", user);
        response.put("token", token);

        // Auditoría de login
        Auditoria audLogin = new Auditoria();
        audLogin.setTablaAccion("usuario");
        audLogin.setAccionAudtria("L");
        audLogin.setUsrioAudtria(user.getLoginUsrio());
        audLogin.setIdTabla(user.getId());
        audLogin.setComentarioAudtria("Login de usuario " + user.getLoginUsrio());
        audLogin.setFchaAudtria(new Date());
        audLogin.setAddressAudtria(Util.getClientIp(request));
        auditoriaServiceAPI.save(audLogin);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
