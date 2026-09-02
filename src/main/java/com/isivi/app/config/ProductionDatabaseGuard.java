package com.isivi.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;

/**
 * Guarda anti-producción para garantizar que ningún test pueda ejecutarse
 * contra la base de datos real de producción ('proyecto').
 */
@Configuration
public class ProductionDatabaseGuard {

    private static final Logger log = LoggerFactory.getLogger(ProductionDatabaseGuard.class);

    @Autowired
    private Environment environment;

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void verifyDatabaseIsNotProductionDuringTests() {
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isTestProfile = Arrays.asList(activeProfiles).contains("test");

        if (isTestProfile) {
            String databaseName = mongoTemplate.getDb().getName();
            log.info("[GUARD] Validando nombre de base de datos activa para tests: '{}'", databaseName);
            
            if ("proyecto".equalsIgnoreCase(databaseName)) {
                log.error("[GUARD] CRÍTICO - TEST ABORTADO: se intentó utilizar la base de datos de producción 'proyecto' en perfil de test.");
                throw new IllegalStateException("TEST ABORTADO: se intentó utilizar la base de datos de producción.");
            }
        }
    }
}
