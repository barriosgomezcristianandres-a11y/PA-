package com.isivi.app.controller;

import com.isivi.app.model.Kit;
import com.isivi.app.repository.KitRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kits")
@CrossOrigin(origins = "*")
public class KitController {

    @Autowired
    private KitRepository kitRepository;

    @Autowired
    private com.isivi.app.repository.ReservaRepository reservaRepository;

    @GetMapping
    public List<Kit> listar() {
        List<Kit> lista = kitRepository.findAll();
        List<com.isivi.app.model.Reserva> activas = reservaRepository.findAll().stream()
                .filter(r -> Boolean.TRUE.equals(r.getInventarioReservado()))
                .filter(r -> "Pendiente Pago".equalsIgnoreCase(r.getEstado()) || "Pendiente Comprobante".equalsIgnoreCase(r.getEstado()))
                .toList();
        for (Kit k : lista) {
            boolean holds = activas.stream().anyMatch(r -> r.getItemsInventario().stream()
                    .anyMatch(i -> "kit".equalsIgnoreCase(i.getTipo()) && k.getId().equals(i.getId())));
            k.setTemporalmenteReservado(holds);
        }
        return lista;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Kit> obtener(@PathVariable String id) {
        return kitRepository.findById(id).map(k -> {
            List<com.isivi.app.model.Reserva> activas = reservaRepository.findAll().stream()
                    .filter(r -> Boolean.TRUE.equals(r.getInventarioReservado()))
                    .filter(r -> "Pendiente Pago".equalsIgnoreCase(r.getEstado()) || "Pendiente Comprobante".equalsIgnoreCase(r.getEstado()))
                    .toList();
            boolean holds = activas.stream().anyMatch(r -> r.getItemsInventario().stream()
                    .anyMatch(i -> "kit".equalsIgnoreCase(i.getTipo()) && k.getId().equals(i.getId())));
            k.setTemporalmenteReservado(holds);
            return ResponseEntity.ok(k);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Kit> crear(@Valid @RequestBody Kit kit) {
        Kit guardado = kitRepository.save(kit);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Kit> actualizar(@PathVariable String id, @Valid @RequestBody Kit datos) {
        return kitRepository.findById(id).map(existente -> {
            existente.setNombre(datos.getNombre());
            existente.setPrecio(datos.getPrecio());
            existente.setPrecioAnterior(datos.getPrecioAnterior());
            existente.setCategoriaId(datos.getCategoriaId());
            existente.setDescripcion(datos.getDescripcion());
            existente.setImagenUrl(datos.getImagenUrl());
            existente.setImageZoom(datos.getImageZoom());
            existente.setImagePosX(datos.getImagePosX());
            existente.setImagePosY(datos.getImagePosY());
            existente.setEnStock(datos.getEnStock());
            existente.setCantidad(datos.getCantidad());
            existente.setOrigenImagen(datos.getOrigenImagen());
            return ResponseEntity.ok(kitRepository.save(existente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Kit> alternarStock(@PathVariable String id) {
        return kitRepository.findById(id).map(existente -> {
            existente.setEnStock(!Boolean.TRUE.equals(existente.getEnStock()));
            return ResponseEntity.ok(kitRepository.save(existente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        if (!kitRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        kitRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
