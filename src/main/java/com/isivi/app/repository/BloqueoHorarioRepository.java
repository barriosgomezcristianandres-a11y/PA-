package com.isivi.app.repository;

import com.isivi.app.model.BloqueoHorario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BloqueoHorarioRepository extends MongoRepository<BloqueoHorario, String> {
    List<BloqueoHorario> findByFechaCita(LocalDate fechaCita);
    Optional<BloqueoHorario> findByFechaCitaAndHoraCita(LocalDate fechaCita, String horaCita);
    boolean existsByFechaCitaAndHoraCita(LocalDate fechaCita, String horaCita);
}
