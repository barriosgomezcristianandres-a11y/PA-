package com.isivi.app.controller;

import com.isivi.app.model.Servicio;
import com.isivi.app.repository.ServicioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "*")
public class ServicioController {

    @Autowired
    private ServicioRepository servicioRepository;

    @GetMapping
    public List<Servicio> listar(@RequestParam(required = false) String categoria) {
        if (categoria != null && !categoria.isBlank() && !categoria.equalsIgnoreCase("todos")) {
            return servicioRepository.findByCategoria(categoria);
        }
        return servicioRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Servicio> obtener(@PathVariable String id) {
        return servicioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Servicio> crear(@Valid @RequestBody Servicio servicio) {
        Servicio guardado = servicioRepository.save(servicio);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Servicio> actualizar(@PathVariable String id, @Valid @RequestBody Servicio datos) {
        return servicioRepository.findById(id).map(existente -> {
            existente.setNombre(datos.getNombre());
            existente.setCategoria(datos.getCategoria());
            existente.setPrecio(datos.getPrecio());
            existente.setDuracion(datos.getDuracion());
            existente.setDescripcion(datos.getDescripcion());
            existente.setImagenUrl(datos.getImagenUrl());
            existente.setImageZoom(datos.getImageZoom());
            existente.setImagePosX(datos.getImagePosX());
            existente.setImagePosY(datos.getImagePosY());
            existente.setOrigenImagen(datos.getOrigenImagen());
            return ResponseEntity.ok(servicioRepository.save(existente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        if (!servicioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        servicioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
