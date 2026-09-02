package com.isivi.app;

import com.isivi.app.model.Reserva;
import com.isivi.app.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PublicPaymentStatusSanitizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservaRepository reservaRepository;

    private Reserva reservaPrueba;

    @BeforeEach
    public void setup() {
        reservaRepository.deleteAll();

        reservaPrueba = new Reserva();
        reservaPrueba.setNombreCliente("Juan Perez");
        reservaPrueba.setTelefono("3112223344");
        reservaPrueba.setEmail("juan@example.com");
        reservaPrueba.setReferenciaWompi("REF-TEST-PUBLIC-SANITIZED");
        reservaPrueba.setTransaccionWompiId("TX-12345");
        reservaPrueba.setEstado("Pendiente Pago");
        reservaPrueba.setEstadoPago("PENDIENTE");
        reservaPrueba.setMontoPagoCentavos(5000000L);
        reservaPrueba.setCodigoReserva("ISV-P123");
        reservaPrueba = reservaRepository.save(reservaPrueba);
    }

    @Test
    public void testSanitizedEndpoints() throws Exception {
        // Test /api/pagos/wompi/estado/{referencia}
        mockMvc.perform(get("/api/pagos/wompi/estado/" + reservaPrueba.getReferenciaWompi()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referencia").value("REF-TEST-PUBLIC-SANITIZED"))
                .andExpect(jsonPath("$.estadoPago").value("PENDIENTE"))
                .andExpect(jsonPath("$.estadoReserva").value("Pendiente Pago"))
                .andExpect(jsonPath("$.codigoReserva").value("ISV-P123"))
                .andExpect(jsonPath("$.montoCentavos").value(5000000L))
                .andExpect(jsonPath("$", not(hasKey("reserva"))));

        // Test /api/pagos/wompi/transaccion/{transactionId}
        mockMvc.perform(get("/api/pagos/wompi/transaccion/" + reservaPrueba.getTransaccionWompiId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referencia").value("REF-TEST-PUBLIC-SANITIZED"))
                .andExpect(jsonPath("$.estadoPago").value("PENDIENTE"))
                .andExpect(jsonPath("$.estadoReserva").value("Pendiente Pago"))
                .andExpect(jsonPath("$.codigoReserva").value("ISV-P123"))
                .andExpect(jsonPath("$.montoCentavos").value(5000000L))
                .andExpect(jsonPath("$", not(hasKey("reserva"))));
    }
}
