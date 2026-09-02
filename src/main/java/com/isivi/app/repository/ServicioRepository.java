package com.isivi.app.repository;

import com.isivi.app.model.Servicio;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ServicioRepository extends MongoRepository<Servicio, String> {
    List<Servicio> findByCategoria(String categoria);
}
