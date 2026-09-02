package com.isivi.app.controller;

import com.isivi.app.model.CategoriaProducto;
import com.isivi.app.repository.CategoriaProductoRepository;
import com.isivi.app.repository.KitRepository;
import com.isivi.app.repository.ProductoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categorias-producto")
@CrossOrigin(origins = "*")
public class CategoriaProductoController {

    private final CategoriaProductoRepository categoriaProductoRepository;
    private final ProductoRepository productoRepository;
    private final KitRepository kitRepository;

    public CategoriaProductoController(CategoriaProductoRepository categoriaProductoRepository, ProductoRepository productoRepository, KitRepository kitRepository) {
        this.categoriaProductoRepository = categoriaProductoRepository;
        this.productoRepository = productoRepository;
        this.kitRepository = kitRepository;
    }

    @GetMapping
    public List<CategoriaProducto> listar(
            @RequestParam(required = false) Boolean soloActivas,
            @RequestParam(required = false) String tipo) {
        if (tipo != null && !tipo.isBlank()) {
            String tipoUpper = tipo.toUpperCase();
            if (Boolean.TRUE.equals(soloActivas)) {
                return categoriaProductoRepository.findActiveByTipoOrNull(tipoUpper);
            }
            return categoriaProductoRepository.findByTipoOrNull(tipoUpper);
        }
        if (Boolean.TRUE.equals(soloActivas)) {
            return categoriaProductoRepository.findByActivoTrue();
        }
        return categoriaProductoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaProducto> obtener(@PathVariable String id) {
        return categoriaProductoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody CategoriaProducto categoria) {
        String nombre = categoria.getNombre() != null ? categoria.getNombre().trim() : "";
        if (nombre.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de la categoría es obligatorio."));
        }
        if (categoriaProductoRepository.existsByNombreIgnoreCase(nombre)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Ya existe una categoría con ese nombre."));
        }

        String tipoVal = categoria.getTipo() != null ? categoria.getTipo().trim().toUpperCase() : "";
        if (tipoVal.isBlank() || !List.of("PRODUCTO", "KIT").contains(tipoVal)) {
            return ResponseEntity.badRequest().body(Map.of("error", "El tipo de categoría debe ser PRODUCTO o KIT."));
        }

        String id = categoria.getId();
        if (id == null || id.isBlank()) {
            id = generarSlug(nombre);
        } else {
            id = id.trim().toLowerCase();
        }

        if (categoriaProductoRepository.existsById(id)) {
            id = id + "-" + System.currentTimeMillis() % 1000;
        }

        categoria.setId(id);
        categoria.setNombre(nombre);
        categoria.setTipo(tipoVal);
        if (categoria.getActivo() == null) categoria.setActivo(true);

        CategoriaProducto guardada = categoriaProductoRepository.save(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @Valid @RequestBody CategoriaProducto datos) {
        Optional<CategoriaProducto> opt = categoriaProductoRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        CategoriaProducto existente = opt.get();
        String nuevoNombre = datos.getNombre() != null ? datos.getNombre().trim() : "";
        if (nuevoNombre.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de la categoría es obligatorio."));
        }

        // Si cambió de nombre, validar que no choque con otra categoría distinta
        Optional<CategoriaProducto> otraConMismoNombre = categoriaProductoRepository.findByNombreIgnoreCase(nuevoNombre);
        if (otraConMismoNombre.isPresent() && !otraConMismoNombre.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Ya existe otra categoría con ese nombre."));
        }

        String nuevoTipo = datos.getTipo() != null ? datos.getTipo().trim().toUpperCase() : "";
        if (!nuevoTipo.isBlank()) {
            if (!List.of("PRODUCTO", "KIT").contains(nuevoTipo)) {
                return ResponseEntity.badRequest().body(Map.of("error", "El tipo de categoría debe ser PRODUCTO o KIT."));
            }
            existente.setTipo(nuevoTipo);
        }

        existente.setNombre(nuevoNombre);
        if (datos.getActivo() != null) {
            existente.setActivo(datos.getActivo());
        }
        return ResponseEntity.ok(categoriaProductoRepository.save(existente));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> alternarActivo(@PathVariable String id) {
        return categoriaProductoRepository.findById(id).map(cat -> {
            cat.setActivo(!Boolean.TRUE.equals(cat.getActivo()));
            return ResponseEntity.ok(categoriaProductoRepository.save(cat));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        if (!categoriaProductoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        long productosAsociados = productoRepository.countByCategoriaId(id);
        long kitsAsociados = kitRepository.countByCategoriaId(id);
        if (productosAsociados > 0 || kitsAsociados > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "error", "CATEGORIA_CON_PRODUCTOS",
                    "mensaje", "La categoría no puede eliminarse porque tiene productos o kits asociados."
            ));
        }
        categoriaProductoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private String generarSlug(String texto) {
        String slug = texto.toLowerCase().trim()
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("[ñ]", "n")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.isBlank() ? "cat-" + System.currentTimeMillis() : slug;
    }
}
