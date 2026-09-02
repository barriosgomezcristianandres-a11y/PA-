package com.isivi.app.repository;

import com.isivi.app.model.Reserva;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservaRepository extends MongoRepository<Reserva, String> {
    List<Reserva> findByFechaCita(LocalDate fechaCita);
    java.util.Optional<Reserva> findByCodigoReservaAndTelefono(String codigoReserva, String telefono);
    java.util.Optional<Reserva> findByCodigoReserva(String codigoReserva);
    List<Reserva> findByTelefonoOrderByFechaRegistroDesc(String telefono);
    List<Reserva> findByEmailOrderByFechaRegistroDesc(String email);
    boolean existsByCodigoReserva(String codigoReserva);
    java.util.Optional<Reserva> findByReferenciaWompi(String referenciaWompi);
    java.util.Optional<Reserva> findByTransaccionWompiId(String transaccionWompiId);
    boolean existsByReferenciaWompi(String referenciaWompi);
    boolean existsByFechaCitaAndHoraCitaAndEstadoNot(LocalDate fechaCita, String horaCita, String estado);
}
