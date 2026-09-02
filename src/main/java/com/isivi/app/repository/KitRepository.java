package com.isivi.app.repository;

import com.isivi.app.model.Kit;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KitRepository extends MongoRepository<Kit, String> {
    long countByCategoriaId(String categoriaId);
}
