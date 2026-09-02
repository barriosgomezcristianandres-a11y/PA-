package com.isivi.app.controller;

import com.isivi.app.model.ExperienciaIsivi;
import com.isivi.app.repository.ExperienciaIsiviRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/experiencias-isivi")
public class ExperienciaIsiviController {

    private final ExperienciaIsiviRepository repository;

    public ExperienciaIsiviController(ExperienciaIsiviRepository repository) {
        this.repository = repository;
    }

    // Endpoint público de solo lectura (Filtrado sin datos privados)
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getExperienciasPublicas() {
        List<ExperienciaIsivi> experiencias = repository.findByActivaTrueAndAutorizacionPublicacionTrueOrderByOrdenAscCreatedAtDesc();
        List<Map<String, Object>> resultado = experiencias.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("nombreMostrar", e.getNombreMostrar());
            map.put("testimonio", e.getTestimonio());
            map.put("calificacion", e.getCalificacion());
            map.put("imagenPrincipal", e.getImagenPrincipal());
            map.put("imagenAntes", e.getImagenAntes());
            map.put("imagenDespues", e.getImagenDespues());
            map.put("tipoRelacionado", e.getTipoRelacionado());
            map.put("elementoRelacionadoId", e.getElementoRelacionadoId());
            map.put("destacada", Boolean.TRUE.equals(e.getDestacada()));
            map.put("orden", e.getOrden() != null ? e.getOrden() : 0);
            map.put("fit", e.getFit());
            map.put("zoom", e.getZoom());
            map.put("posX", e.getPosX());
            map.put("posY", e.getPosY());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    // Endpoint admin: listar todas
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ExperienciaIsivi>> getExperienciasAdmin() {
        return ResponseEntity.ok(repository.findAllByOrderByOrdenAscCreatedAtDesc());
    }

    // Endpoint admin: crear o editar
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearOEliminarExperiencia(@RequestBody ExperienciaIsivi experiencia) {
        // Normalización trim()
        experiencia.setNombreMostrar(experiencia.getNombreMostrar().trim());
        experiencia.setTestimonio(experiencia.getTestimonio().trim());

        if (experiencia.getNombreMostrar().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre a mostrar es obligatorio."));
        }
        if (experiencia.getTestimonio().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El testimonio es obligatorio."));
        }
        if (experiencia.getCalificacion() == null || experiencia.getCalificacion() < 1 || experiencia.getCalificacion() > 5) {
            experiencia.setCalificacion(5);
        }
        if (Boolean.TRUE.equals(experiencia.getActiva()) && !Boolean.TRUE.equals(experiencia.getAutorizacionPublicacion())) {
            return ResponseEntity.badRequest().body(Map.of("error", "No se puede activar una experiencia sin autorización explícita de publicación del cliente."));
        }

        // Validación de seguridad para URLs de imágenes (Bloquear javascript:, data:, vbscript:)
        String[] urls = {experiencia.getImagenPrincipal(), experiencia.getImagenAntes(), experiencia.getImagenDespues()};
        for (String url : urls) {
            if (url != null && !url.isBlank()) {
                String u = url.trim().toLowerCase();
                if (!u.startsWith("http://") && !u.startsWith("https://") && !u.startsWith("/")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Las URLs de imágenes deben comenzar con http://, https:// o una ruta relativa /"));
                }
                if (u.contains("javascript:") || u.contains("vbscript:") || u.contains("data:")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Formato de URL de imagen no permitido por razones de seguridad."));
                }
            }
        }

        if (experiencia.getId() != null && !experiencia.getId().isBlank()) {
            ExperienciaIsivi existente = repository.findById(experiencia.getId()).orElse(null);
            if (existente != null) {
                experiencia.setCreatedAt(existente.getCreatedAt());
            } else {
                experiencia.setCreatedAt(LocalDateTime.now());
            }
        } else {
            experiencia.setId(null);
            experiencia.setCreatedAt(LocalDateTime.now());
        }

        experiencia.setUpdatedAt(LocalDateTime.now());
        ExperienciaIsivi guardada = repository.save(experiencia);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    // Endpoint admin: eliminar
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarExperiencia(@PathVariable String id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Experiencia eliminada correctamente."));
    }
}
