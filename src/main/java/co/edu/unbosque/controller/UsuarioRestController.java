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
import co.edu.unbosque.utils.EmailService;
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
	private EmailService emailService;

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
    public ResponseEntity<?> save(@RequestBody Usuario usuario, HttpServletRequest request) {
        String correoNorm = usuario.getCorreoUsuario().toLowerCase().trim();
        String loginNorm  = usuario.getLoginUsrio() != null ? usuario.getLoginUsrio().toLowerCase().trim() : correoNorm;
        usuario.setCorreoUsuario(correoNorm);
        usuario.setLoginUsrio(loginNorm);

        boolean esNuevo = (usuario.getId() == null);
        if (esNuevo) {
            if (usuarioServiceAPI.findByCorreoUsuario(correoNorm).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("El correo " + correoNorm + " ya está registrado.");
            }
        }

        if (usuario.getFchaUltmaClave() == null) {
            usuario.setFchaUltmaClave(new Date());
        }

        String claveTemporal = null;
        if (esNuevo) {
            claveTemporal = Util.generarClaveTemporal();
            usuario.setClaveUsrio(utilidad.generarHash(usuario, claveTemporal));
            usuario.setIntentos(0);
            usuario.setEstado((byte)1);
            usuario.setIdTipoUsuario("2");
        } else {
            usuario.setClaveUsrio(utilidad.generarHash(usuario, usuario.getClaveUsrio()));
        }

        Usuario obj = usuarioServiceAPI.save(usuario);

        // AUDITORÍA - En registro debe seguir como "anonymous"
        Auditoria aud = new Auditoria();
        aud.setTablaAccion("usuario");
        aud.setAccionAudtria(esNuevo ? "I" : "U");
        aud.setUsrioAudtria("anonymous");
        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria(esNuevo ?
                "Registro de usuario con correo: " + correoNorm :
                "Actualización de usuario con correo: " + correoNorm);
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));
        auditoriaServiceAPI.save(aud);

        if (esNuevo && claveTemporal != null) {
            emailService.enviarClaveTemporal(correoNorm, claveTemporal);
            return ResponseEntity.ok("Registro exitoso. Revisa tu correo para la clave temporal.");
        } else {
            return ResponseEntity.ok("Actualización exitosa.");
        }
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

        if (!user.getClaveUsrio().equals(claveUsuario)) {
            user.setIntentos(user.getIntentos() + 1);
            usuarioServiceAPI.save(user);
            return new ResponseEntity<>("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }

        user.setIntentos(0);
        usuarioServiceAPI.save(user);

        String token = jwtUtil.generateToken(user.getLoginUsrio());

        Map<String, Object> response = new HashMap<>();
        response.put("usuario", user);
        response.put("token", token);

        // AUDITORÍA - aquí ya se usa el correo del usuario
        Auditoria audLogin = new Auditoria();
        audLogin.setTablaAccion("usuario");
        audLogin.setAccionAudtria("L");
        audLogin.setUsrioAudtria(user.getCorreoUsuario()); // correo real
        audLogin.setIdTabla(user.getId());
        audLogin.setComentarioAudtria("Inicio de sesión exitoso para el usuario con correo: " + user.getCorreoUsuario());
        audLogin.setFchaAudtria(new Date());
        audLogin.setAddressAudtria(Util.getClientIp(request));
        auditoriaServiceAPI.save(audLogin);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String correo = payload.get("email").toLowerCase().trim();

        Optional<Usuario> userOpt = usuarioServiceAPI.findByCorreoUsuario(correo);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("El correo no está registrado en el sistema.");
        }
        
        Usuario user = userOpt.get();

        // Generar nueva clave temporal y guardarla (hasheada)
        String nuevaClave = Util.generarClaveTemporal();
        user.setClaveUsrio(utilidad.generarHash(user, nuevaClave));
        user.setFchaUltmaClave(new Date());
        usuarioServiceAPI.save(user);

        // AUDITORÍA - correo real del usuario
        Auditoria aud = new Auditoria();
        aud.setTablaAccion("usuario");
        aud.setAccionAudtria("R");
        aud.setUsrioAudtria(user.getCorreoUsuario());
        aud.setIdTabla(user.getId());
        aud.setComentarioAudtria(
            "Recuperación de contraseña solicitada para el usuario con correo: " + correo
        );
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));
        auditoriaServiceAPI.save(aud);

        // Enviar el correo
        emailService.enviarRecuperacionClave(correo, nuevaClave);

        return ResponseEntity.ok("Se ha enviado una nueva clave temporal a tu correo.");
    }

}