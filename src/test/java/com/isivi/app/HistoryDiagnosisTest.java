package com.isivi.app;

import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ReservaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class HistoryDiagnosisTest {

    @Autowired
    private ReservaRepository reservaRepository;

    @Test
    public void diagnosticarColeccionReservas() {
        List<Reserva> todas = reservaRepository.findAll();
        System.out.println("=================================================");
        System.out.println("DIAGNÓSTICO DE PERSISTENCIA MONGODB - ISIVI");
        System.out.println("=================================================");
        System.out.println("TOTAL RESERVAS EN MONGODB: " + todas.size());

        long archivadas = todas.stream().filter(r -> Boolean.TRUE.equals(r.getArchivada())).count();
        long noArchivadas = todas.stream().filter(r -> !Boolean.TRUE.equals(r.getArchivada())).count();
        long canceladas = todas.stream().filter(r -> "Cancelada".equalsIgnoreCase(r.getEstado())).count();
        long denegadas = todas.stream().filter(r -> "Denegada".equalsIgnoreCase(r.getEstado())).count();
        long confirmadas = todas.stream().filter(r -> "Confirmado".equalsIgnoreCase(r.getEstado())).count();
        long pedidosSinCita = todas.stream().filter(r -> r.getFechaCita() == null).count();

        System.out.println("TOTAL ARCHIVADAS (archivada=true): " + archivadas);
        System.out.println("TOTAL ACTIVAS (archivada=false/null): " + noArchivadas);
        System.out.println("TOTAL CONFIRMADAS: " + confirmadas);
        System.out.println("TOTAL CANCELADAS: " + canceladas);
        System.out.println("TOTAL DENEGADAS: " + denegadas);
        System.out.println("TOTAL PEDIDOS SIN CITA: " + pedidosSinCita);
        System.out.println("-------------------------------------------------");

        for (int i = 0; i < Math.min(todas.size(), 10); i++) {
            Reserva r = todas.get(i);
            System.out.println(String.format("Doc #%d -> ID: %s | Código: %s | Cliente: %s | Estado: %s | Pago: %s | Archivada: %s | Cita: %s | Registro: %s",
                    i + 1, r.getId(), r.getCodigoReserva(), r.getNombreCliente(), r.getEstado(), r.getEstadoPago(), r.getArchivada(), r.getFechaCita(), r.getFechaRegistro()));
        }
        System.out.println("=================================================");
    }
}
