package com.isivi.app.repository;

import com.isivi.app.model.CategoriaProducto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaProductoRepository extends MongoRepository<CategoriaProducto, String> {
    List<CategoriaProducto> findByActivoTrue();
    Optional<CategoriaProducto> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);

    @org.springframework.data.mongodb.repository.Query("{'activo': true, '$or': [{'tipo': ?0}, {'tipo': null}, {'tipo': ''}]}")
    List<CategoriaProducto> findActiveByTipoOrNull(String tipo);

    @org.springframework.data.mongodb.repository.Query("{'$or': [{'tipo': ?0}, {'tipo': null}, {'tipo': ''}]}")
    List<CategoriaProducto> findByTipoOrNull(String tipo);
}
