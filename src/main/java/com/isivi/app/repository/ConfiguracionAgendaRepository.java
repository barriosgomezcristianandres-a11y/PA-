package com.isivi.app.repository;

import com.isivi.app.model.ConfiguracionAgenda;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConfiguracionAgendaRepository extends MongoRepository<ConfiguracionAgenda, String> {
}
