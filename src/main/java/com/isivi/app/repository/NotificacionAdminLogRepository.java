package com.isivi.app.repository;

import com.isivi.app.model.NotificacionAdminLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificacionAdminLogRepository extends MongoRepository<NotificacionAdminLog, String> {
    Optional<NotificacionAdminLog> findByNotifKey(String notifKey);
}
