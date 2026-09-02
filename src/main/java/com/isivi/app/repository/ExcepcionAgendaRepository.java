package com.isivi.app.repository;

import com.isivi.app.model.ExcepcionAgenda;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExcepcionAgendaRepository extends MongoRepository<ExcepcionAgenda, String> {
    Optional<ExcepcionAgenda> findByFecha(LocalDate fecha);
    List<ExcepcionAgenda> findByFechaGreaterThanEqualOrderByFechaAsc(LocalDate fecha);
    boolean existsByFecha(LocalDate fecha);
}
