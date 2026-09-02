package com.isivi.app.repository;

import com.isivi.app.model.Administrador;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AdministradorRepository extends MongoRepository<Administrador, String> {
    Optional<Administrador> findByUsuario(String usuario);
    List<Administrador> findAllByUsuario(String usuario);
    void deleteByUsuario(String usuario);
}

