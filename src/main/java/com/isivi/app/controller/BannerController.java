package com.isivi.app.controller;

import com.isivi.app.model.Banner;
import com.isivi.app.repository.BannerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/banners")
@CrossOrigin(origins = "*")
public class BannerController {
    private final BannerRepository bannerRepository;
    public BannerController(BannerRepository bannerRepository) { this.bannerRepository = bannerRepository; }
    @GetMapping public List<Banner> listar() { return bannerRepository.findAll(); }
    @PostMapping public ResponseEntity<Banner> crear(@RequestBody Banner banner) { return ResponseEntity.status(HttpStatus.CREATED).body(bannerRepository.save(banner)); }
    @PatchMapping("/{id}/activo") public ResponseEntity<Banner> alternarActivo(@PathVariable String id) {
        return bannerRepository.findById(id).map(banner -> { banner.setActivo(Boolean.FALSE.equals(banner.getActivo())); return ResponseEntity.ok(bannerRepository.save(banner)); }).orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<Banner> actualizar(@PathVariable String id, @RequestBody Banner bannerActualizado) {
        return bannerRepository.findById(id).map(banner -> {
            banner.setTitulo(bannerActualizado.getTitulo());
            banner.setDescripcion(bannerActualizado.getDescripcion());
            banner.setImagenUrl(bannerActualizado.getImagenUrl());
            banner.setTextoBoton(bannerActualizado.getTextoBoton());
            banner.setEnlaceBoton(bannerActualizado.getEnlaceBoton());
            banner.setActivo(bannerActualizado.getActivo());
            banner.setTipoDestino(bannerActualizado.getTipoDestino());
            banner.setDestinoId(bannerActualizado.getDestinoId());
            banner.setImageZoom(bannerActualizado.getImageZoom());
            banner.setImagePosX(bannerActualizado.getImagePosX());
            banner.setImagePosY(bannerActualizado.getImagePosY());
            return ResponseEntity.ok(bannerRepository.save(banner));
        }).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable String id) {
        if (!bannerRepository.existsById(id)) return ResponseEntity.notFound().build();
        bannerRepository.deleteById(id); return ResponseEntity.noContent().build();
    }
}
