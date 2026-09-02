package com.isivi.app.controller;

import com.isivi.app.model.Producto;
import com.isivi.app.model.VarianteProducto;
import com.isivi.app.repository.ProductoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private com.isivi.app.repository.ReservaRepository reservaRepository;

    @GetMapping
    public List<Producto> listar(@RequestParam(required = false) String categoria) {
        List<Producto> lista;
        if (categoria != null && !categoria.isBlank()) {
            lista = productoRepository.findByCategoriaId(categoria);
        } else {
            lista = productoRepository.findAll();
        }
        
        List<com.isivi.app.model.Reserva> activas = reservaRepository.findAll().stream()
                .filter(r -> Boolean.TRUE.equals(r.getInventarioReservado()))
                .filter(r -> "Pendiente Pago".equalsIgnoreCase(r.getEstado()) || "Pendiente Comprobante".equalsIgnoreCase(r.getEstado()))
                .toList();

        for (Producto p : lista) {
            boolean esVar = "VARIANTES".equalsIgnoreCase(p.getTipoPrecio());
            if (esVar) {
                for (VarianteProducto v : p.getVariantes()) {
                    boolean holds = activas.stream().anyMatch(r -> r.getItemsInventario().stream()
                            .anyMatch(i -> "producto".equalsIgnoreCase(i.getTipo()) && p.getId().equals(i.getId()) && v.getId().equals(i.getVarianteId())));
                    v.setTemporalmenteReservado(holds);
                }
                boolean algunHold = p.getVariantes().stream().anyMatch(VarianteProducto::getTemporalmenteReservado);
                p.setTemporalmenteReservado(algunHold);
            } else {
                boolean holds = activas.stream().anyMatch(r -> r.getItemsInventario().stream()
                        .anyMatch(i -> "producto".equalsIgnoreCase(i.getTipo()) && p.getId().equals(i.getId())));
                p.setTemporalmenteReservado(holds);
            }
        }
        return lista;
    }

    public List<Producto> listar() {
        return listar(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable String id) {
        return productoRepository.findById(id).map(p -> {
            List<com.isivi.app.model.Reserva> activas = reservaRepository.findAll().stream()
                    .filter(r -> Boolean.TRUE.equals(r.getInventarioReservado()))
                    .filter(r -> "Pendiente Pago".equalsIgnoreCase(r.getEstado()) || "Pendiente Comprobante".equalsIgnoreCase(r.getEstado()))
                    .toList();

            boolean esVar = "VARIANTES".equalsIgnoreCase(p.getTipoPrecio());
            if (esVar) {
                for (VarianteProducto v : p.getVariantes()) {
                    boolean holds = activas.stream().anyMatch(r -> r.getItemsInventario().stream()
                            .anyMatch(i -> "producto".equalsIgnoreCase(i.getTipo()) && p.getId().equals(i.getId()) && v.getId().equals(i.getVarianteId())));
                    v.setTemporalmenteReservado(holds);
                }
                boolean algunHold = p.getVariantes().stream().anyMatch(VarianteProducto::getTemporalmenteReservado);
                p.setTemporalmenteReservado(algunHold);
            } else {
                boolean holds = activas.stream().anyMatch(r -> r.getItemsInventario().stream()
                        .anyMatch(i -> "producto".equalsIgnoreCase(i.getTipo()) && p.getId().equals(i.getId())));
                p.setTemporalmenteReservado(holds);
            }
            return ResponseEntity.ok(p);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        sanearProducto(producto);
        Producto guardado = productoRepository.save(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable String id, @Valid @RequestBody Producto datos) {
        return productoRepository.findById(id).map(existente -> {
            sanearProducto(datos);
            existente.setNombre(datos.getNombre());
            existente.setPrecio(datos.getPrecio());
            existente.setPrecioAnterior(datos.getPrecioAnterior());
            existente.setTipoPrecio(datos.getTipoPrecio());
            existente.setCategoriaId(datos.getCategoriaId());
            existente.setVariantes(datos.getVariantes());
            existente.setDescripcion(datos.getDescripcion());
            existente.setImagenUrl(datos.getImagenUrl());
            existente.setImageZoom(datos.getImageZoom());
            existente.setImagePosX(datos.getImagePosX());
            existente.setImagePosY(datos.getImagePosY());
            existente.setEnStock(datos.getEnStock());
            existente.setCantidad(datos.getCantidad());
            existente.setOrigenImagen(datos.getOrigenImagen());
            return ResponseEntity.ok(productoRepository.save(existente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Producto> alternarStock(@PathVariable String id) {
        return productoRepository.findById(id).map(existente -> {
            boolean nuevoEstado = !Boolean.TRUE.equals(existente.getEnStock());
            existente.setEnStock(nuevoEstado);
            if ("VARIANTES".equalsIgnoreCase(existente.getTipoPrecio()) && existente.getVariantes() != null) {
                existente.getVariantes().forEach(v -> v.setEnStock(nuevoEstado));
            }
            return ResponseEntity.ok(productoRepository.save(existente));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        if (!productoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void sanearProducto(Producto p) {
        if (p.getTipoPrecio() == null || p.getTipoPrecio().isBlank()) {
            p.setTipoPrecio("UNICO");
        } else {
            p.setTipoPrecio(p.getTipoPrecio().trim().toUpperCase());
        }

        if ("VARIANTES".equalsIgnoreCase(p.getTipoPrecio())) {
            List<VarianteProducto> vars = p.getVariantes();
            if (vars == null || vars.isEmpty()) {
                p.setTipoPrecio("UNICO");
            } else {
                int totalStock = 0;
                double minPrecio = Double.MAX_VALUE;
                boolean algunEnStock = false;

                for (VarianteProducto v : vars) {
                    if (v.getId() == null || v.getId().isBlank()) {
                        v.setId("var-" + UUID.randomUUID().toString().substring(0, 8));
                    }
                    if (v.getNombre() == null || v.getNombre().isBlank()) {
                        v.setNombre("Variante");
                    }
                    if (v.getPrecio() == null || v.getPrecio() < 0) {
                        v.setPrecio(p.getPrecio() != null && p.getPrecio() > 0 ? p.getPrecio() : 0.0);
                    }
                    if (v.getCantidad() == null || v.getCantidad() < 0) {
                        v.setCantidad(0);
                    }
                    if (v.getActivo() == null) {
                        v.setActivo(true);
                    }
                    if (v.getEnStock() == null) {
                        v.setEnStock(v.getCantidad() > 0);
                    }

                    if (Boolean.TRUE.equals(v.getActivo())) {
                        totalStock += v.getCantidad();
                        if (v.getPrecio() < minPrecio) {
                            minPrecio = v.getPrecio();
                        }
                        if (Boolean.TRUE.equals(v.getEnStock()) && v.getCantidad() > 0) {
                            algunEnStock = true;
                        }
                    }
                }
                p.setCantidad(totalStock);
                p.setPrecio(minPrecio != Double.MAX_VALUE ? minPrecio : (p.getPrecio() != null ? p.getPrecio() : 0.0));
                p.setEnStock(algunEnStock || totalStock > 0);
            }
        }
    }
}
