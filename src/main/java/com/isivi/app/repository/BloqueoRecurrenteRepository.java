package com.isivi.app.repository;

import com.isivi.app.model.BloqueoRecurrente;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BloqueoRecurrenteRepository extends MongoRepository<BloqueoRecurrente, String> {
    List<BloqueoRecurrente> findByActivoTrue();
}
