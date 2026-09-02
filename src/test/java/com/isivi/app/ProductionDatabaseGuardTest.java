package com.isivi.app;

import com.isivi.app.config.ProductionDatabaseGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ProductionDatabaseGuardTest {

    @Autowired
    private Environment environment;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    @DisplayName("Caso A: perfil = test y database = isivi_test -> PASS")
    void testCaseA_ProfileTestAndTestDb_Pass() {
        // En ambiente de test, la base de datos configurada debe ser isivi_test
        String databaseName = mongoTemplate.getDb().getName();
        assertEquals("isivi_test", databaseName, "La base de datos de test debe llamarse 'isivi_test'");
        
        // Ejecutar guard directamente para ver que no lanza excepción
        ProductionDatabaseGuard guard = new ProductionDatabaseGuard();
        ReflectionTestUtils.setField(guard, "environment", environment);
        ReflectionTestUtils.setField(guard, "mongoTemplate", mongoTemplate);
        
        assertDoesNotThrow(guard::verifyDatabaseIsNotProductionDuringTests);
    }

    @Test
    @DisplayName("Caso B: perfil = test y database = proyecto -> FAIL/BLOCK")
    void testCaseB_ProfileTestAndProductionDb_Fail() {
        Environment mockEnv = Mockito.mock(Environment.class);
        Mockito.when(mockEnv.getActiveProfiles()).thenReturn(new String[]{"test"});

        com.mongodb.client.MongoDatabase mockDb = Mockito.mock(com.mongodb.client.MongoDatabase.class);
        Mockito.when(mockDb.getName()).thenReturn("proyecto");

        MongoTemplate mockMongo = Mockito.mock(MongoTemplate.class);
        Mockito.when(mockMongo.getDb()).thenReturn(mockDb);

        ProductionDatabaseGuard guard = new ProductionDatabaseGuard();
        ReflectionTestUtils.setField(guard, "environment", mockEnv);
        ReflectionTestUtils.setField(guard, "mongoTemplate", mockMongo);

        IllegalStateException ex = assertThrows(IllegalStateException.class, guard::verifyDatabaseIsNotProductionDuringTests);
        assertTrue(ex.getMessage().contains("TEST ABORTADO: se intentó utilizar la base de datos de producción"));
    }

    @Test
    @DisplayName("Caso C: Aislamiento conceptual de base de datos")
    void testCaseC_AislamientoConceptual() {
        String dbName = mongoTemplate.getDb().getName();
        assertNotEquals("proyecto", dbName, "La base de datos de pruebas jamás debe ser 'proyecto'");
    }
}
