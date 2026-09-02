package com.isivi.app.repository;

import com.isivi.app.model.ConfiguracionNegocio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionNegocioRepository extends MongoRepository<ConfiguracionNegocio, String> {
}
