package com.isivi.app.controller;

import com.isivi.app.repository.AdministradorRepository;
import com.isivi.app.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AdministradorRepository administradorRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credenciales) {
        String usuario = credenciales.getOrDefault("usuario", "").trim();
        String contrasena = credenciales.getOrDefault("contrasena", "");
        boolean autenticado = administradorRepository.findByUsuario(usuario)
                .map(admin -> passwordEncoder.matches(contrasena, admin.getContrasenaHash()))
                .orElse(false);

        if (!autenticado) {
            return ResponseEntity.ok(Map.of("autenticado", false));
        }

        String token = jwtService.generarToken(usuario);
        return ResponseEntity.ok(Map.of(
                "autenticado", true,
                "token", token,
                "usuario", usuario
        ));
    }

    @PostMapping("/administradores")
    public ResponseEntity<Map<String, Object>> crearAdministrador(@RequestBody Map<String, String> datos) {
        String usuario = datos.getOrDefault("usuario", "").trim();
        String contrasena = datos.getOrDefault("contrasena", "");

        if (usuario.length() < 3 || contrasena.length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El usuario debe tener 3 caracteres y la contraseña 4."));
        }
        if (administradorRepository.findByUsuario(usuario).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", "Ese usuario ya existe."));
        }

        var administrador = administradorRepository.save(
                new com.isivi.app.model.Administrador(usuario, passwordEncoder.encode(contrasena)));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", administrador.getId(), "usuario", administrador.getUsuario()));
    }

    @GetMapping("/administradores")
    public List<Map<String, String>> listarAdministradores() {
        return administradorRepository.findAll().stream()
                .map(administrador -> Map.of("id", administrador.getId(), "usuario", administrador.getUsuario()))
                .toList();
    }

    @DeleteMapping("/administradores/{id}")
    public ResponseEntity<?> eliminarAdministrador(@PathVariable String id) {
        if (!administradorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (administradorRepository.count() <= 1) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Debe permanecer al menos un administrador activo."));
        }
        administradorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
