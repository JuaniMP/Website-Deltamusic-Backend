// src/main/java/co/edu/unbosque/controller/UsuarioRestController.java

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
        // 1) Normalizar correo y login
        String correoNorm = usuario.getCorreoUsuario().toLowerCase().trim();
        String loginNorm  = usuario.getLoginUsrio() != null 
                             ? usuario.getLoginUsrio().toLowerCase().trim() 
                             : correoNorm;
        usuario.setCorreoUsuario(correoNorm);
        usuario.setLoginUsrio(loginNorm);

        // 2) Detectar si es INSERT o UPDATE
        boolean esNuevo = (usuario.getId() == null);
        if (esNuevo) {
            // Si existe otro usuario con el mismo correo, rechazamos
            if (usuarioServiceAPI.findByCorreoUsuario(correoNorm).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("El correo " + correoNorm + " ya está registrado.");
            }
        }

        // 3) Si nunca tenía fecha de última clave, la asignamos hoy
        if (usuario.getFchaUltmaClave() == null) {
            usuario.setFchaUltmaClave(new Date());
        }

        // 4) Preparar contra si viene INSERT
        String claveTemporal = null;
        if (esNuevo) {
            // Generamos contraseña temporal
            claveTemporal = Util.generarClaveTemporal();
            usuario.setClaveUsrio(utilidad.generarHash(usuario, claveTemporal));
            usuario.setIntentos(0);
            usuario.setEstado((byte) 1);
            usuario.setIdTipoUsuario("2");  // Por defecto “2”=cliente normal
        } else {
            // Si es UPDATE, venimos con usuario.getClaveUsrio() en texto plano
            // Lo hasheamos y actualizamos fchaUltmaClave
            usuario.setClaveUsrio(utilidad.generarHash(usuario, usuario.getClaveUsrio()));
            usuario.setFchaUltmaClave(new Date());
        }

        // 5) Guardar o actualizar
        Usuario obj = usuarioServiceAPI.save(usuario);

        // 6) Crear registro de auditoría
        Auditoria aud = new Auditoria();
        aud.setTablaAccion("usuario");
        aud.setAccionAudtria(esNuevo ? "I" : "U");
        aud.setUsrioAudtria("anonymous");  // o usa request.getRemoteAddr(), según convenga
        aud.setIdTabla(obj.getId());
        aud.setComentarioAudtria(esNuevo 
                ? "Registro de usuario con correo: " + correoNorm
                : "Actualización de usuario con correo: " + correoNorm);
        aud.setFchaAudtria(new Date());
        aud.setAddressAudtria(Util.getClientIp(request));
        auditoriaServiceAPI.save(aud);

        // 7) Si es INSERT mando mail y devuelvo el usuario completo
        if (esNuevo && claveTemporal != null) {
            emailService.enviarClaveTemporal(correoNorm, claveTemporal);
            return ResponseEntity.ok(obj);
        } else {
            // 8) Para UPDATE, devolvemos el objeto Usuario actualizado (JSON)
            return ResponseEntity.ok(obj);
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

        // 1. Verificar si está bloqueado (solo aplica a tipo usuario 2)
        if ("2".equals(user.getIdTipoUsuario()) && user.getEstado() == 0) {
            return new ResponseEntity<>("Cuenta bloqueada. Debes recuperar tu contraseña.", HttpStatus.FORBIDDEN);
        }

        String claveUsuario = utilidad.generarHash(user, loginRequest.getClave());

        // 2. Validar contraseña
        if (!user.getClaveUsrio().equals(claveUsuario)) {
            // Solo usuarios normales (no admin ni otros)
            if ("2".equals(user.getIdTipoUsuario())) {
                int intentos = user.getIntentos() + 1;
                user.setIntentos(intentos);

                // Si llegó a 3 intentos, bloquear cuenta
                if (intentos >= 3) {
                    user.setEstado((byte) 0); // 0 = bloqueado
                }
                usuarioServiceAPI.save(user);

                if (intentos >= 3) {
                    return new ResponseEntity<>("Cuenta bloqueada tras 3 intentos fallidos. Debes recuperar tu contraseña.", HttpStatus.FORBIDDEN);
                }
            }
            return new ResponseEntity<>("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }

        // Si llegó aquí, login exitoso: reiniciar intentos y activar usuario
        user.setIntentos(0);
        // Por si acaso, si la cuenta estaba bloqueada y la contraseña es correcta (no debería pasar), reactivar
        if ("2".equals(user.getIdTipoUsuario())) {
            user.setEstado((byte) 1);
        }
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
    public ResponseEntity<?> forgotPassword(
            @RequestBody Map<String, String> payload,
            HttpServletRequest request) {

        // 1) Intentar leer primero "correoUsuario", si no viene, "email"
        String correoRaw = payload.get("correoUsuario");
        if (correoRaw == null) {
            correoRaw = payload.get("email");
        }

        // 2) Validar que venga algo
        if (correoRaw == null || correoRaw.isBlank()) {
            return ResponseEntity
                .badRequest()
                .body("Debes enviar 'correoUsuario' o 'email' con un valor válido.");
        }

        // 3) Normalizar correo
        String correo = correoRaw.toLowerCase().trim();

        // 4) Comprobar existencia de usuario
        Optional<Usuario> userOpt = usuarioServiceAPI.findByCorreoUsuario(correo);
        if (userOpt.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("El correo no está registrado en el sistema.");
        }
        Usuario user = userOpt.get();

        // 5) Generar y guardar nueva clave temporal (hasheada)
        String nuevaClave = Util.generarClaveTemporal();
        user.setClaveUsrio(utilidad.generarHash(user, nuevaClave));
        user.setFchaUltmaClave(new Date());
        // Reiniciar intentos y reactivar cuenta solo si es usuario tipo 2
        if ("2".equals(user.getIdTipoUsuario())) {
            user.setIntentos(0);
            user.setEstado((byte) 1); // Activo
        }
        usuarioServiceAPI.save(user);

        // 6) Registrar auditoría
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

        // 7) Enviar el correo con la nueva clave
        emailService.enviarRecuperacionClave(correo, nuevaClave);

        // 8) Responder OK
        return ResponseEntity
            .ok("Se ha enviado una nueva clave temporal a tu correo.");
    }
}
