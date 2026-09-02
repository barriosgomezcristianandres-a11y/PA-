package com.isivi.app.repository;

import com.isivi.app.model.Producto;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductoRepository extends MongoRepository<Producto, String> {
    List<Producto> findByCategoriaId(String categoriaId);
    long countByCategoriaId(String categoriaId);
}

