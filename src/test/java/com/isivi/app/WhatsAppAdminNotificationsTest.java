package com.isivi.app;

import com.isivi.app.model.ConfiguracionNegocio;
import com.isivi.app.model.NotificacionAdminLog;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ConfiguracionNegocioRepository;
import com.isivi.app.repository.NotificacionAdminLogRepository;
import com.isivi.app.service.NotificacionAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WhatsAppAdminNotificationsTest {

    @Autowired
    private NotificacionAdminLogRepository logRepository;

    @Autowired
    private ConfiguracionNegocioRepository configRepository;

    @Autowired
    private NotificacionAdminService adminService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        logRepository.deleteAll();
        
        // Habilitar notificaciones administrativas por defecto para pruebas
        ConfiguracionNegocio cfg = configRepository.findById("principal").orElseGet(ConfiguracionNegocio::new);
        cfg.setWhatsappAdminHabilitado(true);
        cfg.setWhatsappAdminNumero("+573008949050");
        cfg.setNotificarNuevaCita(true);
        cfg.setNotificarNuevaCompra(true);
        cfg.setNotificarConflictoPago(true);
        configRepository.save(cfg);
    }

    @Test
    @DisplayName("1. Dos inserciones simultáneas con mismo notifKey resultan en un solo documento")
    void testAtomicIndexUniqueNotifKey() {
        String key = "TEST_UNIQUE_KEY:" + UUID.randomUUID();
        
        NotificacionAdminLog l1 = new NotificacionAdminLog();
        l1.setNotifKey(key);
        l1.setEstado("PENDING");
        l1.setCreadoEn(Instant.now());
        
        NotificacionAdminLog l2 = new NotificacionAdminLog();
        l2.setNotifKey(key);
        l2.setEstado("PENDING");
        l2.setCreadoEn(Instant.now());

        // La primera inserción pasa
        assertDoesNotThrow(() -> mongoTemplate.insert(l1));
        
        // La segunda lanza DuplicateKeyException
        assertThrows(DuplicateKeyException.class, () -> mongoTemplate.insert(l2));
        
        // El repositorio contiene exactamente un documento
        long count = mongoTemplate.count(new Query(Criteria.where("notifKey").is(key)), NotificacionAdminLog.class);
        assertEquals(1, count);
    }

    @Test
    @DisplayName("2. Un worker reclama el documento de forma atómica (Claim Atómico)")
    void testAtomicWorkerClaim() {
        String key = "CLAIM_KEY:" + UUID.randomUUID();
        NotificacionAdminLog l1 = new NotificacionAdminLog();
        l1.setNotifKey(key);
        l1.setEstado("PENDING");
        l1.setCreadoEn(Instant.now());
        mongoTemplate.insert(l1);

        // Intento de reclamo por Worker 1
        Query claimQuery = new Query(Criteria.where("notifKey").is(key).and("estado").is("PENDING"));
        Update claimUpdate = new Update().set("estado", "SENDING").set("actualizadoEn", Instant.now());
        NotificacionAdminLog claimed1 = mongoTemplate.findAndModify(claimQuery, claimUpdate, NotificacionAdminLog.class);
        
        assertNotNull(claimed1);
        assertEquals("PENDING", claimed1.getEstado());

        // Intento de reclamo por Worker 2
        NotificacionAdminLog claimed2 = mongoTemplate.findAndModify(claimQuery, claimUpdate, NotificacionAdminLog.class);
        assertNull(claimed2); // Worker 2 no puede reclamarlo
    }

    @Test
    @DisplayName("3. El scheduler recupera registros en SENDING huérfanos (>10 minutos)")
    void testSendingRecovery() {
        String key = "ORPHAN_KEY:" + UUID.randomUUID();
        NotificacionAdminLog l1 = new NotificacionAdminLog();
        l1.setNotifKey(key);
        l1.setEstado("SENDING");
        l1.setCreadoEn(Instant.now().minus(15, ChronoUnit.MINUTES));
        l1.setActualizadoEn(Instant.now().minus(15, ChronoUnit.MINUTES));
        mongoTemplate.insert(l1);

        adminService.ejecutarSchedulerRecuperacion();

        NotificacionAdminLog recuperado = logRepository.findByNotifKey(key).orElse(null);
        assertNotNull(recuperado);
        assertEquals("PENDING", recuperado.getEstado());
    }

    @Test
    @DisplayName("4. Si WhatsApp está deshabilitado no se guarda notificación")
    void testDisabledAdminNotifications() {
        ConfiguracionNegocio cfg = configRepository.findById("principal").orElseGet(ConfiguracionNegocio::new);
        cfg.setWhatsappAdminHabilitado(false);
        configRepository.save(cfg);

        Reserva r = new Reserva();
        r.setId("reserva-dummy-disabled");
        r.setNombreCliente("Juan");
        r.setTelefono("3001112233");
        r.setSubtotal(50000.0);

        adminService.registrarNuevaCita(r);
        
        long count = logRepository.count();
        assertEquals(0, count);
    }
}
