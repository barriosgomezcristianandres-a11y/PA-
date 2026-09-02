package com.isivi.app.controller;

import com.isivi.app.model.CategoriaServicio;
import com.isivi.app.repository.CategoriaServicioRepository;
import com.isivi.app.repository.ServicioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categorias-servicio")
@CrossOrigin(origins = "*")
public class CategoriaServicioController {
    private final CategoriaServicioRepository categoriaRepository;
    private final ServicioRepository servicioRepository;
    public CategoriaServicioController(CategoriaServicioRepository categoriaRepository, ServicioRepository servicioRepository) {
        this.categoriaRepository = categoriaRepository; this.servicioRepository = servicioRepository;
    }
    @GetMapping public List<CategoriaServicio> listar() { return categoriaRepository.findAll(); }
    @PostMapping public ResponseEntity<?> crear(@Valid @RequestBody CategoriaServicio categoria) {
        String id = categoria.getId() == null ? "" : categoria.getId().trim().toLowerCase();
        if (!id.matches("[a-z0-9-]+")) return ResponseEntity.badRequest().body("El identificador de categoría no es válido.");
        if (categoriaRepository.existsById(id)) return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe una categoría con ese nombre.");
        categoria.setId(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaRepository.save(categoria));
    }
    @DeleteMapping("/{id}") public ResponseEntity<?> eliminar(@PathVariable String id) {
        if (!categoriaRepository.existsById(id)) return ResponseEntity.notFound().build();
        if (!servicioRepository.findByCategoria(id).isEmpty()) return ResponseEntity.status(HttpStatus.CONFLICT).body("La categoría tiene servicios asignados.");
        categoriaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
