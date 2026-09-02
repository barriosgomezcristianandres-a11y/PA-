package com.isivi.app.repository;

import com.isivi.app.model.ExperienciaIsivi;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienciaIsiviRepository extends MongoRepository<ExperienciaIsivi, String> {
    List<ExperienciaIsivi> findByActivaTrueAndAutorizacionPublicacionTrueOrderByOrdenAscCreatedAtDesc();
    List<ExperienciaIsivi> findAllByOrderByOrdenAscCreatedAtDesc();
}
