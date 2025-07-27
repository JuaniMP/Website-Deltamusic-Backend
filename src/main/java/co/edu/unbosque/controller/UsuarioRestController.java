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
        // Normalizar campos clave
        String correoNorm = usuario.getCorreoUsuario().toLowerCase().trim();
        String loginNorm  = usuario.getLoginUsrio() != null
                            ? usuario.getLoginUsrio().toLowerCase().trim()
                            : correoNorm;
        usuario.setCorreoUsuario(correoNorm);
        usuario.setLoginUsrio(loginNorm);

        // Verificar duplicado SOLO en registro nuevo
        boolean esNuevo = (usuario.getId() == null);
        if (esNuevo) {
            if (usuarioServiceAPI.findByCorreoUsuario(correoNorm).isPresent()) {
                // Ya existe un usuario con este correo
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("El correo " + correoNorm + " ya está registrado.");
            }
        }

        // Fecha de última clave
        if (usuario.getFchaUltmaClave() == null) {
            usuario.setFchaUltmaClave(new Date());
        }

        // Generar hash de la clave
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

        // Guardar usuario
        Usuario obj = usuarioServiceAPI.save(usuario);

        // Auditoría
        Auditoria aud = new Auditoria();
        aud.setTablaAccion("usuario");
        aud.setAccionAudtria(esNuevo ? "I" : "U");
        aud.setUsrioAudtria("anonymous");
        aud.setIdTabla(obj.getId());
        String prefix = esNuevo ? "Registro usuario: " : "Actualización usuario: ";
        String ident = correoNorm;
        int maxLen = 60 - prefix.length();
        String idRec = ident.length() > maxLen
                      ? ident.substring(0, maxLen - 3) + "..."
                      : ident;
        aud.setComentarioAudtria(prefix + idRec);
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));
        auditoriaServiceAPI.save(aud);

        // Envío de correo en registro
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

      
        Auditoria aud = new Auditoria();
        aud.setTablaAccion("usuario");
        aud.setAccionAudtria("R"); 
        aud.setUsrioAudtria("anonymous");
        aud.setIdTabla(user.getId());
        String prefix = "Recuperación clave usuario: ";
        int maxLen = 60 - prefix.length();
        String correoRec = correo.length() > maxLen ? correo.substring(0, maxLen - 3) + "..." : correo;
        aud.setComentarioAudtria(prefix + correoRec);
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));
        auditoriaServiceAPI.save(aud);

        // Enviar el correo
        emailService.enviarRecuperacionClave(correo, nuevaClave);

        return ResponseEntity.ok("Se ha enviado una nueva clave temporal a tu correo.");
    }

    


    

}