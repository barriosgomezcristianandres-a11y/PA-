package com.isivi.app.controller;

import com.isivi.app.model.Kit;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Reserva;
import com.isivi.app.repository.KitRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ReservaRepository;
import com.isivi.app.util.HorarioUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final ReservaRepository reservaRepository;
    private final ProductoRepository productoRepository;
    private final KitRepository kitRepository;
    private final com.isivi.app.service.ReservaService reservaService;
    private Clock clock = Clock.system(ZoneId.of("America/Bogota"));

    public DashboardController(ReservaRepository reservaRepository, ProductoRepository productoRepository,
                               KitRepository kitRepository, @org.springframework.beans.factory.annotation.Autowired(required = false) com.isivi.app.service.ReservaService reservaService) {
        this.reservaRepository = reservaRepository;
        this.productoRepository = productoRepository;
        this.kitRepository = kitRepository;
        this.reservaService = reservaService;
        this.clock = Clock.system(ZoneId.of("America/Bogota"));
    }

    public void setClock(Clock clock) {
        this.clock = clock != null ? clock : Clock.system(ZoneId.of("America/Bogota"));
        if (this.reservaService != null) {
            this.reservaService.setClock(this.clock);
        }
    }


    @GetMapping("/resumen")
    public ResponseEntity<Map<String, Object>> obtenerResumen() {
        Clock effectiveClock = this.clock != null ? this.clock : Clock.system(ZoneId.of("America/Bogota"));
        ZoneId zone = effectiveClock.getZone();
        LocalDate hoy = LocalDate.now(effectiveClock);
        LocalDateTime ahora = LocalDateTime.now(effectiveClock);
        LocalDate inicioSemana = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        List<Reserva> todasReservas = reservaRepository.findAll();
        List<Producto> todosProductos = productoRepository.findAll();
        List<Kit> todosKits = kitRepository.findAll();

        // 1. Citas de hoy (reservas activas con fecha de cita hoy, excluyendo las ya finalizadas según horaInicio + duración)
        List<Reserva> agendaHoy = todasReservas.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getArchivada()))
                .filter(r -> r.esCita() && hoy.equals(r.getFechaCita()))
                .filter(r -> !"Cancelada".equalsIgnoreCase(r.getEstado()) && !"Denegada".equalsIgnoreCase(r.getEstado()) && !"Expirada".equalsIgnoreCase(r.getEstado()))
                .sorted(Comparator.comparing(r -> {
                    try {
                        LocalTime lt = HorarioUtil.parseTimeSlot(r.getHoraCita());
                        return lt != null ? lt : LocalTime.MAX;
                    } catch (Exception e) {
                        return LocalTime.MAX;
                    }
                }))
                .map(r -> {
                    if (reservaService != null && reservaService.citaFinalizada(r)) {
                        r.setEstado("Realizada");
                    } else if (reservaService != null && reservaService.citaEnCurso(r)) {
                        r.setEstado("En curso");
                    }
                    return r;
                })
                .toList();

        long citasHoyCount = agendaHoy.size();
        long citasConfirmadas = agendaHoy.stream().filter(r -> "Confirmado".equalsIgnoreCase(r.getEstado())).count();
        long citasEnCurso = agendaHoy.stream().filter(r -> "En curso".equalsIgnoreCase(r.getEstado())).count();
        long citasPendientes = agendaHoy.stream().filter(r -> r.getEstado() != null && r.getEstado().toLowerCase().startsWith("pendiente")).count();
        long citasCompletadas = todasReservas.stream()
                .filter(r -> r.esCita() && hoy.equals(r.getFechaCita()))
                .filter(r -> "Completada".equalsIgnoreCase(r.getEstado()) || "Realizada".equalsIgnoreCase(r.getEstado()) || (reservaService != null && reservaService.citaFinalizada(r)))
                .count();

        Map<String, Object> citasHoyDesglose = new LinkedHashMap<>();
        citasHoyDesglose.put("total", citasHoyCount);
        citasHoyDesglose.put("confirmadas", citasConfirmadas);
        citasHoyDesglose.put("enCurso", citasEnCurso);
        citasHoyDesglose.put("pendientes", citasPendientes);
        citasHoyDesglose.put("completadas", citasCompletadas);


        // 2. Ventas reales (pagos aprobados o anticipos de reservas confirmadas)
        List<Reserva> ventasHoyLista = todasReservas.stream()
                .filter(this::esVentaValida)
                .filter(r -> esFechaDeVenta(r, hoy))
                .toList();

        double ventasHoy = ventasHoyLista.stream().mapToDouble(this::montoVenta).sum();
        long transaccionesHoy = ventasHoyLista.size();
        double ticketPromedioHoy = transaccionesHoy > 0 ? Math.round(ventasHoy / transaccionesHoy) : 0.0;

        double productosHoy = 0.0;
        double serviciosHoy = 0.0;
        for (Reserva r : ventasHoyLista) {
            double totalR = montoVenta(r);
            double prodR = montoProductos(r);
            if (prodR > totalR) prodR = totalR;
            productosHoy += prodR;
            serviciosHoy += Math.max(0.0, totalR - prodR);
        }

        LocalDate ayer = hoy.minusDays(1);
        double ventasAyer = todasReservas.stream()
                .filter(this::esVentaValida)
                .filter(r -> esFechaDeVenta(r, ayer))
                .mapToDouble(this::montoVenta)
                .sum();

        Double cambioVsAyerPct = null;
        if (ventasAyer > 0) {
            cambioVsAyerPct = Math.round(((ventasHoy - ventasAyer) / ventasAyer) * 1000.0) / 10.0;
        } else if (ventasHoy > 0) {
            cambioVsAyerPct = 100.0;
        }

        Map<String, Object> ventasHoyDetalle = new LinkedHashMap<>();
        ventasHoyDetalle.put("total", ventasHoy);
        ventasHoyDetalle.put("ventasAyer", ventasAyer);
        ventasHoyDetalle.put("cambioVsAyerPct", cambioVsAyerPct);
        ventasHoyDetalle.put("transaccionesHoy", transaccionesHoy);
        ventasHoyDetalle.put("ticketPromedioHoy", ticketPromedioHoy);
        ventasHoyDetalle.put("serviciosHoy", serviciosHoy);
        ventasHoyDetalle.put("productosHoy", productosHoy);

        List<Reserva> ventasSemanaLista = todasReservas.stream()
                .filter(this::esVentaValida)
                .filter(r -> estaEnRangoVenta(r, inicioSemana, hoy))
                .toList();

        double ventasSemana = ventasSemanaLista.stream().mapToDouble(this::montoVenta).sum();
        long transaccionesSemana = ventasSemanaLista.size();
        double ticketPromedioSemana = transaccionesSemana > 0 ? Math.round(ventasSemana / transaccionesSemana) : 0.0;

        double productosSemana = 0.0;
        double serviciosSemana = 0.0;
        for (Reserva r : ventasSemanaLista) {
            double totalR = montoVenta(r);
            double prodR = montoProductos(r);
            if (prodR > totalR) prodR = totalR;
            productosSemana += prodR;
            serviciosSemana += Math.max(0.0, totalR - prodR);
        }

        Map<String, Object> ventasSemanaDetalle = new LinkedHashMap<>();
        ventasSemanaDetalle.put("total", ventasSemana);
        ventasSemanaDetalle.put("transaccionesSemana", transaccionesSemana);
        ventasSemanaDetalle.put("ticketPromedioSemana", ticketPromedioSemana);
        ventasSemanaDetalle.put("serviciosSemana", serviciosSemana);
        ventasSemanaDetalle.put("productosSemana", productosSemana);

        List<Reserva> ventasMesLista = todasReservas.stream()
                .filter(this::esVentaValida)
                .filter(r -> estaEnRangoVenta(r, inicioMes, hoy))
                .toList();

        double ventasMes = ventasMesLista.stream().mapToDouble(this::montoVenta).sum();
        long transaccionesMes = ventasMesLista.size();
        double ticketPromedioMes = transaccionesMes > 0 ? Math.round(ventasMes / transaccionesMes) : 0.0;

        double productosMes = 0.0;
        double serviciosMes = 0.0;
        for (Reserva r : ventasMesLista) {
            double totalR = montoVenta(r);
            double prodR = montoProductos(r);
            if (prodR > totalR) prodR = totalR;
            productosMes += prodR;
            serviciosMes += Math.max(0.0, totalR - prodR);
        }

        Map<String, Object> ventasMesDetalle = new LinkedHashMap<>();
        ventasMesDetalle.put("total", ventasMes);
        ventasMesDetalle.put("transaccionesMes", transaccionesMes);
        ventasMesDetalle.put("ticketPromedioMes", ticketPromedioMes);
        ventasMesDetalle.put("serviciosMes", serviciosMes);
        ventasMesDetalle.put("productosMes", productosMes);

        // 3. Solicitudes y novedades pendientes
        List<Reserva> pendientesLista = todasReservas.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getArchivada()))
                .filter(r -> {
                    String est = r.getEstado() != null ? r.getEstado().trim() : "";
                    String medio = r.getMedioPago() != null ? r.getMedioPago().trim().toUpperCase() : "";
                    String estPago = r.getEstadoPago() != null ? r.getEstadoPago().trim().toUpperCase() : "";

                    // Excluir transacciones Wompi fallidas/rechazadas/error/voided de la bandeja operativa
                    if ("WOMPI".equals(medio) && (
                            "DECLINED".equals(estPago) ||
                            "ERROR".equals(estPago) ||
                            "VOIDED".equals(estPago) ||
                            "RECHAZADO".equals(estPago)
                    )) {
                        return false;
                    }

                    if ("WOMPI".equals(medio) && "Pendiente Pago".equalsIgnoreCase(est)) {
                        return false;
                    }
                    return "Solicitud Cancelación".equalsIgnoreCase(est)
                            || "Solicitud Cancelacion".equalsIgnoreCase(est)
                            || "Pendiente Reprogramación".equalsIgnoreCase(est)
                            || "Pendiente Reprogramacion".equalsIgnoreCase(est)
                            || "Pendiente Comprobante".equalsIgnoreCase(est)
                            || "Pendiente Pago".equalsIgnoreCase(est);
                })
                .sorted(Comparator.comparingInt(r -> prioridadEstado(r.getEstado())))
                .toList();

        long pendientesCount = pendientesLista.size();
        long solicitudesCancelacion = pendientesLista.stream().filter(r -> r.getEstado() != null && (r.getEstado().equalsIgnoreCase("Solicitud Cancelación") || r.getEstado().equalsIgnoreCase("Solicitud Cancelacion"))).count();
        long reprogPendientes = pendientesLista.stream().filter(r -> r.getEstado() != null && (r.getEstado().equalsIgnoreCase("Pendiente Reprogramación") || r.getEstado().equalsIgnoreCase("Pendiente Reprogramacion"))).count();
        long compPendientes = pendientesLista.stream().filter(r -> r.getEstado() != null && r.getEstado().equalsIgnoreCase("Pendiente Comprobante")).count();
        long pagosPendientes = pendientesLista.stream().filter(r -> r.getEstado() != null && r.getEstado().equalsIgnoreCase("Pendiente Pago")).count();


        List<Reserva> cancelacionesClienteNoVistas = todasReservas.stream()
                .filter(r -> "Cancelada".equalsIgnoreCase(r.getEstado()))
                .filter(r -> "CLIENTE".equalsIgnoreCase(r.getCanceladaPor()))
                .filter(r -> Boolean.FALSE.equals(r.getNotificacionCancelacionVista()))
                .toList();
        long cancelacionesNoVistas = cancelacionesClienteNoVistas.size();

        List<Reserva> solicitudesCancelacionLista = todasReservas.stream()
                .filter(r -> "Solicitud Cancelación".equalsIgnoreCase(r.getEstado()))
                .toList();

        Map<String, Object> pendientesDesglose = new LinkedHashMap<>();
        pendientesDesglose.put("total", pendientesCount);
        pendientesDesglose.put("solicitudesCancelacion", solicitudesCancelacion);
        pendientesDesglose.put("reprogramaciones", reprogPendientes);
        pendientesDesglose.put("comprobantes", compPendientes);
        pendientesDesglose.put("pagos", pagosPendientes);

        // 4. Productos y Kits por reponer (stock bajo: cantidad <= 3 o enStock == false)
        List<Map<String, Object>> stockBajoLista = new ArrayList<>();
        int porReponerCount = 0;
        int agotadosCount = 0;

        for (Producto p : todosProductos) {
            int cant = p.getCantidad() != null ? p.getCantidad() : 0;
            boolean agotado = Boolean.FALSE.equals(p.getEnStock()) || cant <= 3;
            if (agotado) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", p.getId());
                item.put("nombre", p.getNombre());
                item.put("tipo", "Producto");
                item.put("cantidad", cant);
                item.put("enStock", p.getEnStock());
                item.put("precio", p.getPrecio());
                item.put("imagenUrl", p.getImagenUrl());
                stockBajoLista.add(item);

                if (cant <= 0 || Boolean.FALSE.equals(p.getEnStock())) {
                    agotadosCount++;
                } else {
                    porReponerCount++;
                }
            }
        }

        for (Kit k : todosKits) {
            int cant = k.getCantidad() != null ? k.getCantidad() : 0;
            boolean agotado = Boolean.FALSE.equals(k.getEnStock()) || cant <= 3;
            if (agotado) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", k.getId());
                item.put("nombre", k.getNombre());
                item.put("tipo", "Kit");
                item.put("cantidad", cant);
                item.put("enStock", k.getEnStock());
                item.put("precio", k.getPrecio());
                item.put("imagenUrl", k.getImagenUrl());
                stockBajoLista.add(item);

                if (cant <= 0 || Boolean.FALSE.equals(k.getEnStock())) {
                    agotadosCount++;
                } else {
                    porReponerCount++;
                }
            }
        }

        long stockBajoCount = stockBajoLista.size();

        Map<String, Object> stockBajoDesglose = new LinkedHashMap<>();
        stockBajoDesglose.put("total", stockBajoCount);
        stockBajoDesglose.put("porReponer", porReponerCount);
        stockBajoDesglose.put("agotados", agotadosCount);

        // 5. Historial de ventas últimos 7 días
        List<Map<String, Object>> ventas7Dias = new ArrayList<>();
        String[] diasNombre = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};

        for (int i = 6; i >= 0; i--) {
            LocalDate dia = hoy.minusDays(i);
            double totalDia = todasReservas.stream()
                    .filter(this::esVentaValida)
                    .filter(r -> esFechaDeVenta(r, dia))
                    .mapToDouble(this::montoVenta)
                    .sum();

            Map<String, Object> diaStat = new HashMap<>();
            diaStat.put("fecha", dia.toString());
            diaStat.put("dia", diasNombre[dia.getDayOfWeek().getValue() % 7]);
            diaStat.put("total", totalDia);
            ventas7Dias.add(diaStat);
        }

        // 6. Alertas Operativas de Atención Inmediata
        List<Map<String, Object>> alertasAtencion = new ArrayList<>();
        if (solicitudesCancelacion > 0) {
            List<Map<String, Object>> itemsSolicitud = solicitudesCancelacionLista.stream().map(r -> {
                Map<String, Object> it = new LinkedHashMap<>();
                it.put("id", r.getId());
                it.put("codigo", r.getCodigoReserva());
                it.put("cliente", r.getNombreCliente());
                it.put("telefono", r.getTelefono());
                it.put("fechaCita", r.getFechaCita() != null ? r.getFechaCita().toString() : null);
                it.put("horaCita", r.getHoraCita());
                it.put("estado", r.getEstado());
                it.put("motivoCancelacion", r.getMotivoCancelacion());
                it.put("esPedido", r.esPedidoPuro());
                return it;
            }).toList();

            Map<String, Object> alerta = new LinkedHashMap<>();
            alerta.put("id", "alert-solicitud-cancel");
            alerta.put("tipo", "solicitud_cancelacion");
            alerta.put("icono", "fa-calendar-xmark");
            alerta.put("titulo", "Solicitudes de cancelación por revisar");
            alerta.put("cantidad", solicitudesCancelacion);
            alerta.put("mensaje", solicitudesCancelacion + (solicitudesCancelacion == 1 ? " cliente solicitó cancelar su cita" : " clientes solicitaron cancelar su cita"));
            alerta.put("accionTab", "bookings");
            alerta.put("modulo", "bookings");
            alerta.put("entidad", "RESERVA");
            alerta.put("filtro", "solicitud_cancelacion");
            alerta.put("accion", "DETALLE_CANCELACION");
            if (!itemsSolicitud.isEmpty()) {
                alerta.put("targetId", itemsSolicitud.get(0).get("id"));
                alerta.put("targetCodigo", itemsSolicitud.get(0).get("codigo"));
                alerta.put("targetNombre", itemsSolicitud.get(0).get("cliente"));
                alerta.put("esPedido", itemsSolicitud.get(0).get("esPedido"));
            }
            alerta.put("items", itemsSolicitud);
            alertasAtencion.add(alerta);
        }

        if (cancelacionesNoVistas > 0) {
            List<Map<String, Object>> itemsCancel = cancelacionesClienteNoVistas.stream().map(r -> {
                Map<String, Object> it = new LinkedHashMap<>();
                it.put("id", r.getId());
                it.put("codigo", r.getCodigoReserva());
                it.put("cliente", r.getNombreCliente());
                it.put("telefono", r.getTelefono());
                it.put("fechaCita", r.getFechaCita() != null ? r.getFechaCita().toString() : null);
                it.put("horaCita", r.getHoraCita());
                it.put("estado", r.getEstado());
                it.put("motivoCancelacion", r.getMotivoCancelacion());
                it.put("esPedido", r.esPedidoPuro());
                return it;
            }).toList();

            Map<String, Object> alerta = new LinkedHashMap<>();
            alerta.put("id", "alert-cancelacion-cliente");
            alerta.put("tipo", "cancelacion_cliente");
            alerta.put("icono", "fa-ban");
            alerta.put("titulo", "Cancelaciones recientes de clientes");
            alerta.put("cantidad", cancelacionesNoVistas);
            alerta.put("mensaje", cancelacionesNoVistas + (cancelacionesNoVistas == 1 ? " cita cancelada directamente por cliente" : " citas canceladas directamente por clientes"));
            alerta.put("accionTab", "history");
            alerta.put("modulo", "history");
            alerta.put("entidad", "RESERVA");
            alerta.put("filtro", "canceladas");
            alerta.put("accion", "DETALLE_HISTORIAL");
            if (!itemsCancel.isEmpty()) {
                alerta.put("targetId", itemsCancel.get(0).get("id"));
                alerta.put("targetCodigo", itemsCancel.get(0).get("codigo"));
                alerta.put("targetNombre", itemsCancel.get(0).get("cliente"));
                alerta.put("esPedido", itemsCancel.get(0).get("esPedido"));
            }
            alerta.put("items", itemsCancel);
            alertasAtencion.add(alerta);
        }

        if (reprogPendientes > 0) {
            List<Map<String, Object>> itemsReprog = pendientesLista.stream()
                    .filter(r -> r.getEstado() != null && (r.getEstado().equalsIgnoreCase("Pendiente Reprogramación") || r.getEstado().equalsIgnoreCase("Pendiente Reprogramacion")))
                    .map(r -> {
                        Map<String, Object> it = new LinkedHashMap<>();
                        it.put("id", r.getId());
                        it.put("codigo", r.getCodigoReserva());
                        it.put("cliente", r.getNombreCliente());
                        it.put("telefono", r.getTelefono());
                        it.put("fechaCita", r.getFechaCita() != null ? r.getFechaCita().toString() : null);
                        it.put("horaCita", r.getHoraCita());
                        it.put("estado", r.getEstado());
                        it.put("esPedido", r.esPedidoPuro());
                        return it;
                    }).toList();

            Map<String, Object> alerta = new LinkedHashMap<>();
            alerta.put("id", "alert-reprog");
            alerta.put("tipo", "reprogramacion");
            alerta.put("icono", "fa-calendar-days");
            alerta.put("titulo", "Reprogramaciones pendientes");
            alerta.put("cantidad", reprogPendientes);
            alerta.put("mensaje", reprogPendientes + (reprogPendientes == 1 ? " cliente solicitó reprogramar su cita" : " clientes solicitaron reprogramar su cita"));
            alerta.put("accionTab", "bookings");
            alerta.put("modulo", "bookings");
            alerta.put("entidad", "RESERVA");
            alerta.put("filtro", "reprogramacion");
            alerta.put("accion", "REPROGRAMAR_CITA");
            if (!itemsReprog.isEmpty()) {
                alerta.put("targetId", itemsReprog.get(0).get("id"));
                alerta.put("targetCodigo", itemsReprog.get(0).get("codigo"));
                alerta.put("targetNombre", itemsReprog.get(0).get("cliente"));
                alerta.put("esPedido", itemsReprog.get(0).get("esPedido"));
            }
            alerta.put("items", itemsReprog);
            alertasAtencion.add(alerta);
        }

        if (compPendientes > 0) {
            List<Map<String, Object>> itemsComp = pendientesLista.stream()
                    .filter(r -> r.getEstado() != null && r.getEstado().equalsIgnoreCase("Pendiente Comprobante"))
                    .map(r -> {
                        Map<String, Object> it = new LinkedHashMap<>();
                        it.put("id", r.getId());
                        it.put("codigo", r.getCodigoReserva());
                        it.put("cliente", r.getNombreCliente());
                        it.put("telefono", r.getTelefono());
                        it.put("fechaCita", r.getFechaCita() != null ? r.getFechaCita().toString() : null);
                        it.put("horaCita", r.getHoraCita());
                        it.put("estado", r.getEstado());
                        it.put("esPedido", r.esPedidoPuro());
                        return it;
                    }).toList();

            Map<String, Object> alerta = new LinkedHashMap<>();
            alerta.put("id", "alert-comp");
            alerta.put("tipo", "comprobante");
            alerta.put("icono", "fa-triangle-exclamation");
            alerta.put("titulo", "Comprobantes por validar");
            alerta.put("cantidad", compPendientes);
            alerta.put("mensaje", compPendientes + (compPendientes == 1 ? " transferencia pendiente de comprobante" : " transferencias pendientes de comprobante"));
            alerta.put("accionTab", "bookings");
            alerta.put("modulo", "bookings");
            alerta.put("entidad", "RESERVA");
            alerta.put("filtro", "comprobante");
            alerta.put("accion", "VALIDAR_COMPROBANTE");
            if (!itemsComp.isEmpty()) {
                alerta.put("targetId", itemsComp.get(0).get("id"));
                alerta.put("targetCodigo", itemsComp.get(0).get("codigo"));
                alerta.put("targetNombre", itemsComp.get(0).get("cliente"));
                alerta.put("esPedido", itemsComp.get(0).get("esPedido"));
            }
            alerta.put("items", itemsComp);
            alertasAtencion.add(alerta);
        }

        // Alerta de transacciones aprobadas tardíamente (Conflicto Pago Expirado)
        List<Reserva> conflictosPago = todasReservas.stream()
                .filter(r -> "CONFLICTO_PAGO_EXPIRADO".equalsIgnoreCase(r.getEstado()))
                .toList();
        long conflictosCount = conflictosPago.size();
        if (conflictosCount > 0) {
            List<Map<String, Object>> itemsConflict = conflictosPago.stream().map(r -> {
                Map<String, Object> it = new LinkedHashMap<>();
                it.put("id", r.getId());
                it.put("codigo", r.getCodigoReserva());
                it.put("cliente", r.getNombreCliente());
                it.put("telefono", r.getTelefono());
                it.put("fechaCita", r.getFechaCita() != null ? r.getFechaCita().toString() : null);
                it.put("horaCita", r.getHoraCita());
                it.put("estado", r.getEstado());
                it.put("esPedido", r.esPedidoPuro());
                return it;
            }).toList();

            Map<String, Object> alerta = new LinkedHashMap<>();
            alerta.put("id", "alert-conflicto-pago");
            alerta.put("tipo", "conflicto_pago_expirado");
            alerta.put("icono", "fa-triangle-exclamation");
            alerta.put("titulo", "Conflictos de pago expirado");
            alerta.put("cantidad", conflictosCount);
            alerta.put("mensaje", conflictosCount + (conflictosCount == 1 ? " reserva pagada con horario ya ocupado" : " reservas pagadas con horario ya ocupado"));
            alerta.put("accionTab", "bookings");
            alerta.put("modulo", "bookings");
            alerta.put("entidad", "RESERVA");
            alerta.put("filtro", "todos");
            alerta.put("accion", "DETALLE_RESERVA");
            if (!itemsConflict.isEmpty()) {
                alerta.put("targetId", itemsConflict.get(0).get("id"));
                alerta.put("targetCodigo", itemsConflict.get(0).get("codigo"));
                alerta.put("targetNombre", itemsConflict.get(0).get("cliente"));
                alerta.put("esPedido", itemsConflict.get(0).get("esPedido"));
            }
            alerta.put("items", itemsConflict);
            alertasAtencion.add(alerta);
        }

        if (agotadosCount > 0) {
            List<Map<String, Object>> itemsAgotados = stockBajoLista.stream()
                    .filter(item -> {
                        int cant = item.get("cantidad") != null ? ((Number) item.get("cantidad")).intValue() : 0;
                        Boolean inStock = (Boolean) item.get("enStock");
                        return cant <= 0 || Boolean.FALSE.equals(inStock);
                    })
                    .map(item -> {
                        Map<String, Object> it = new LinkedHashMap<>(item);
                        return it;
                    }).toList();

            Map<String, Object> alerta = new LinkedHashMap<>();
            alerta.put("id", "alert-agotado");
            alerta.put("tipo", "agotado");
            alerta.put("icono", "fa-boxes-packing");
            alerta.put("titulo", "Productos agotados");
            alerta.put("cantidad", agotadosCount);
            alerta.put("mensaje", agotadosCount + (agotadosCount == 1 ? " ítem sin existencias en catálogo" : " ítems sin existencias en catálogo"));
            alerta.put("accionTab", "products");
            alerta.put("modulo", "products");
            alerta.put("entidad", "PRODUCTO");
            alerta.put("filtro", "agotados");
            alerta.put("accion", "EDITAR_PRODUCTO");
            if (!itemsAgotados.isEmpty()) {
                alerta.put("targetId", itemsAgotados.get(0).get("id"));
                alerta.put("targetNombre", itemsAgotados.get(0).get("nombre"));
                alerta.put("targetTipo", itemsAgotados.get(0).get("tipo"));
            }
            alerta.put("items", itemsAgotados);
            alertasAtencion.add(alerta);
        }



        // 6.b Pedidos Activos (Pedidos que requieren acción operativa)
        List<Reserva> pedidosActivosLista = todasReservas.stream()
                .filter(r -> r.esPedidoPuro())
                .filter(r -> !Boolean.TRUE.equals(r.getArchivada()))
                .filter(r -> {
                    String est = r.getEstado() != null ? r.getEstado().trim() : "";
                    String estPago = r.getEstadoPago() != null ? r.getEstadoPago().trim().toUpperCase() : "";

                    if ("Cancelada".equalsIgnoreCase(est)
                            || "Cancelado".equalsIgnoreCase(est)
                            || "Denegada".equalsIgnoreCase(est)
                            || "Expirada".equalsIgnoreCase(est)
                            || "Entregado".equalsIgnoreCase(est)
                            || "Recogido".equalsIgnoreCase(est)) {
                        return false;
                    }

                    if ("Pendiente Pago".equalsIgnoreCase(est)) {
                        return false;
                    }
                    if (List.of("RECHAZADO", "DECLINED", "VOIDED", "ERROR").contains(estPago)) {
                        return false;
                    }

                    return true;
                })
                .sorted(Comparator.comparing((Reserva r) -> r.getFechaRegistro() != null ? r.getFechaRegistro() : LocalDate.MIN).reversed())
                .toList();

        long pedidosActivosCount = pedidosActivosLista.size();
        long pedidosEnPreparacion = pedidosActivosLista.stream().filter(r -> "En preparación".equalsIgnoreCase(r.getEstado())).count();
        long pedidosListosRecoger = pedidosActivosLista.stream().filter(r -> "Listo para recoger".equalsIgnoreCase(r.getEstado())).count();
        long pedidosPagoConfirmado = pedidosActivosLista.stream().filter(r -> "Pago Confirmado".equalsIgnoreCase(r.getEstado()) || "Confirmado".equalsIgnoreCase(r.getEstado())).count();
        long pedidosPendientesPago = pedidosActivosLista.stream().filter(r -> r.getEstado() != null && r.getEstado().toLowerCase().startsWith("pendiente")).count();

        Map<String, Object> pedidosActivosDesglose = new LinkedHashMap<>();
        pedidosActivosDesglose.put("total", pedidosActivosCount);
        pedidosActivosDesglose.put("enPreparacion", pedidosEnPreparacion);
        pedidosActivosDesglose.put("listosParaRecoger", pedidosListosRecoger);
        pedidosActivosDesglose.put("pagoConfirmado", pedidosPagoConfirmado);
        pedidosActivosDesglose.put("pendientes", pedidosPendientesPago);

        if (pedidosListosRecoger > 0) {
            List<Map<String, Object>> itemsListos = pedidosActivosLista.stream()
                    .filter(r -> "Listo para recoger".equalsIgnoreCase(r.getEstado()))
                    .map(r -> {
                        Map<String, Object> it = new LinkedHashMap<>();
                        it.put("id", r.getId());
                        it.put("codigo", r.getCodigoReserva());
                        it.put("cliente", r.getNombreCliente());
                        it.put("telefono", r.getTelefono());
                        it.put("estado", r.getEstado());
                        it.put("esPedido", true);
                        return it;
                    }).toList();

            Map<String, Object> alerta = new LinkedHashMap<>();
            alerta.put("id", "alert-pedidos-listos");
            alerta.put("tipo", "pedido_listo");
            alerta.put("icono", "fa-bag-shopping");
            alerta.put("titulo", "Pedidos listos para recoger");
            alerta.put("cantidad", pedidosListosRecoger);
            alerta.put("mensaje", pedidosListosRecoger + (pedidosListosRecoger == 1 ? " pedido listo para entregar en el local" : " pedidos listos para entregar en el local"));
            alerta.put("accionTab", "orders");
            alerta.put("modulo", "orders");
            alerta.put("entidad", "PEDIDO");
            alerta.put("filtro", "LISTO PARA RECOGER");
            alerta.put("accion", "DETALLE_PEDIDO");
            if (!itemsListos.isEmpty()) {
                alerta.put("targetId", itemsListos.get(0).get("id"));
                alerta.put("targetCodigo", itemsListos.get(0).get("codigo"));
                alerta.put("targetNombre", itemsListos.get(0).get("cliente"));
            }
            alerta.put("items", itemsListos);
            alertasAtencion.add(alerta);
        }

        if (pedidosPagoConfirmado > 0) {
            List<Map<String, Object>> itemsPreparar = pedidosActivosLista.stream()
                    .filter(r -> "Pago Confirmado".equalsIgnoreCase(r.getEstado()) || "Confirmado".equalsIgnoreCase(r.getEstado()))
                    .map(r -> {
                        Map<String, Object> it = new LinkedHashMap<>();
                        it.put("id", r.getId());
                        it.put("codigo", r.getCodigoReserva());
                        it.put("cliente", r.getNombreCliente());
                        it.put("telefono", r.getTelefono());
                        it.put("estado", r.getEstado());
                        it.put("esPedido", true);
                        return it;
                    }).toList();

            Map<String, Object> alerta = new LinkedHashMap<>();
            alerta.put("id", "alert-pedidos-preparar");
            alerta.put("tipo", "pedido_por_preparar");
            alerta.put("icono", "fa-boxes-packing");
            alerta.put("titulo", "Pedidos pagados por preparar");
            alerta.put("cantidad", pedidosPagoConfirmado);
            alerta.put("mensaje", pedidosPagoConfirmado + (pedidosPagoConfirmado == 1 ? " pedido nuevo pendiente de preparación" : " pedidos nuevos pendientes de preparación"));
            alerta.put("accionTab", "orders");
            alerta.put("modulo", "orders");
            alerta.put("entidad", "PEDIDO");
            alerta.put("filtro", "PAGO CONFIRMADO");
            alerta.put("accion", "PREPARAR_PEDIDO");
            if (!itemsPreparar.isEmpty()) {
                alerta.put("targetId", itemsPreparar.get(0).get("id"));
                alerta.put("targetCodigo", itemsPreparar.get(0).get("codigo"));
                alerta.put("targetNombre", itemsPreparar.get(0).get("cliente"));
            }
            alerta.put("items", itemsPreparar);
            alertasAtencion.add(alerta);
        }

        // 7. PRÓXIMAS CITAS (Top 3 a 5 citas activas cronológicas con tiempo restante)
        List<Map<String, Object>> proximasCitas = todasReservas.stream()
                .filter(r -> r.esCita())
                .filter(r -> !Boolean.TRUE.equals(r.getArchivada()))
                .filter(r -> r.getFechaCita() != null && r.getHoraCita() != null && !r.getHoraCita().isBlank())
                .filter(r -> !"Cancelada".equalsIgnoreCase(r.getEstado())
                        && !"Denegada".equalsIgnoreCase(r.getEstado())
                        && !"Expirada".equalsIgnoreCase(r.getEstado())
                        && !"Solicitud Cancelación".equalsIgnoreCase(r.getEstado())
                        && (reservaService == null || !reservaService.citaFinalizada(r)))
                .filter(r -> {
                    LocalTime lt = HorarioUtil.parseTimeSlot(r.getHoraCita());
                    if (lt == null) return false;
                    LocalDateTime ldt = LocalDateTime.of(r.getFechaCita(), lt);
                    return ldt.isAfter(ahora);
                })
                .sorted(Comparator.comparing(r -> {
                    LocalTime lt = HorarioUtil.parseTimeSlot(r.getHoraCita());
                    return LocalDateTime.of(r.getFechaCita(), lt != null ? lt : LocalTime.MAX);
                }))
                .limit(5)
                .map(r -> {
                    LocalTime lt = HorarioUtil.parseTimeSlot(r.getHoraCita());
                    LocalDateTime ldt = LocalDateTime.of(r.getFechaCita(), lt);
                    long minutosRestantes = Duration.between(ahora, ldt).toMinutes();

                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", r.getId());
                    map.put("codigoReserva", r.getCodigoReserva());
                    map.put("nombreCliente", r.getNombreCliente());
                    map.put("telefono", r.getTelefono());
                    map.put("fechaCita", r.getFechaCita().toString());
                    map.put("horaCita", r.getHoraCita());
                    map.put("servicios", r.getItems() != null ? r.getItems() : List.of());
                    map.put("estado", r.getEstado());
                    map.put("minutosRestantes", Math.max(0, minutosRestantes));
                    map.put("tiempoRestanteTexto", formatearTiempoRestante(minutosRestantes));
                    return map;
                })
                .toList();

        Map<String, Object> proximaCita = proximasCitas.isEmpty() ? null : proximasCitas.get(0);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("fechaActual", hoy.toString());
        respuesta.put("citasHoy", citasHoyCount);
        respuesta.put("citasHoyDesglose", citasHoyDesglose);
        respuesta.put("pedidosActivos", pedidosActivosCount);
        respuesta.put("pedidosActivosDesglose", pedidosActivosDesglose);
        respuesta.put("pedidosActivosLista", pedidosActivosLista);
        respuesta.put("ventasHoy", ventasHoy);
        respuesta.put("ventasHoyDetalle", ventasHoyDetalle);
        respuesta.put("ventasSemana", ventasSemana);
        respuesta.put("ventasSemanaDetalle", ventasSemanaDetalle);
        respuesta.put("ventasMes", ventasMes);
        respuesta.put("ventasMesDetalle", ventasMesDetalle);
        respuesta.put("pendientes", pendientesCount);
        respuesta.put("pendientesDesglose", pendientesDesglose);
        respuesta.put("solicitudesCancelacion", solicitudesCancelacion);
        respuesta.put("solicitudesCancelacionLista", solicitudesCancelacionLista);
        respuesta.put("cancelacionesNoVistas", cancelacionesNoVistas);
        respuesta.put("cancelacionesNoVistasLista", cancelacionesClienteNoVistas);
        respuesta.put("stockBajo", stockBajoCount);
        respuesta.put("stockBajoDesglose", stockBajoDesglose);
        respuesta.put("alertasAtencion", alertasAtencion);
        respuesta.put("agendaHoy", agendaHoy);
        respuesta.put("pendientesLista", pendientesLista);
        respuesta.put("stockBajoLista", stockBajoLista);
        respuesta.put("ventas7Dias", ventas7Dias);
        respuesta.put("proximasCitas", proximasCitas);
        respuesta.put("proximaCita", proximaCita);


        return ResponseEntity.ok(respuesta);
    }

    public static String formatearTiempoRestante(long minutos) {
        if (minutos < 1) {
            return "Faltan menos de 1 min";
        }
        if (minutos < 60) {
            return "Faltan " + minutos + " min";
        }
        if (minutos < 1440) {
            long horas = minutos / 60;
            long mins = minutos % 60;
            return mins > 0 ? "Faltan " + horas + " h " + mins + " min" : "Faltan " + horas + " h";
        }
        long dias = minutos / 1440;
        long horas = (minutos % 1440) / 60;
        return horas > 0 ? "Faltan " + dias + (dias == 1 ? " día " : " días ") + horas + " h" : "Faltan " + dias + (dias == 1 ? " día" : " días");
    }

    private double montoProductos(Reserva r) {
        if (r.getItemsInventario() == null || r.getItemsInventario().isEmpty()) {
            return 0.0;
        }
        return r.getItemsInventario().stream()
                .filter(i -> "producto".equalsIgnoreCase(i.getTipo()) || "kit".equalsIgnoreCase(i.getTipo()))
                .mapToDouble(i -> {
                    if (i.getSubtotal() != null && i.getSubtotal() > 0) return i.getSubtotal();
                    int cant = i.getCantidad() != null ? i.getCantidad() : 1;
                    double precio = i.getPrecioUnitario() != null ? i.getPrecioUnitario() : 0.0;
                    return cant * precio;
                })
                .sum();
    }

    private boolean esVentaValida(Reserva r) {
        String est = r.getEstado() != null ? r.getEstado().trim() : "";
        String estPago = r.getEstadoPago() != null ? r.getEstadoPago().trim().toUpperCase() : "";

        // Exclusiones explícitas
        if ("Cancelada".equalsIgnoreCase(est)
                || "Denegada".equalsIgnoreCase(est)
                || "Expirada".equalsIgnoreCase(est)
                || "Solicitud Cancelación".equalsIgnoreCase(est)
                || "Pendiente Pago".equalsIgnoreCase(est)
                || "Pendiente Comprobante".equalsIgnoreCase(est)) {
            return false;
        }

        // Coherencia: Pago debe estar aprobado y el estado debe ser uno de los permitidos operativos
        boolean pagoAprobado = "APROBADO".equals(estPago) || "APPROVED".equals(estPago);
        boolean estadoOperativo = "Confirmado".equalsIgnoreCase(est)
                || "Pago Confirmado".equalsIgnoreCase(est)
                || "En preparación".equalsIgnoreCase(est)
                || "Listo para recoger".equalsIgnoreCase(est)
                || "Listo para envío".equalsIgnoreCase(est)
                || "En camino".equalsIgnoreCase(est)
                || "Recogido".equalsIgnoreCase(est)
                || "Entregado".equalsIgnoreCase(est)
                || "En curso".equalsIgnoreCase(est)
                || "Realizada".equalsIgnoreCase(est);

        return pagoAprobado && estadoOperativo;
    }

    private double montoVenta(Reserva r) {
        String estPago = r.getEstadoPago() != null ? r.getEstadoPago().trim().toUpperCase() : "";
        if (r.getMontoPagoCentavos() != null && r.getMontoPagoCentavos() > 0 && ("APROBADO".equals(estPago) || "APPROVED".equals(estPago))) {
            return r.getMontoPagoCentavos() / 100.0;
        }
        if (r.getAnticipo() != null && r.getAnticipo() > 0) {
            return r.getAnticipo();
        }
        return r.getSubtotal() != null ? r.getSubtotal() : 0.0;
    }

    private LocalDate fechaEfectiva(Reserva r) {
        if (r.getFechaPago() != null) return r.getFechaPago();
        if (r.getFechaRegistro() != null) return r.getFechaRegistro();
        return r.getFechaCita();
    }

    private boolean esFechaDeVenta(Reserva r, LocalDate target) {
        LocalDate f = fechaEfectiva(r);
        return f != null && f.equals(target);
    }

    private boolean estaEnRangoVenta(Reserva r, LocalDate desde, LocalDate hasta) {
        LocalDate f = fechaEfectiva(r);
        return f != null && !f.isBefore(desde) && !f.isAfter(hasta);
    }

    private int prioridadEstado(String estado) {
        if ("Solicitud Cancelación".equalsIgnoreCase(estado)) return 0;
        if ("Pendiente Reprogramación".equalsIgnoreCase(estado)) return 1;
        if ("Pendiente Pago".equalsIgnoreCase(estado)) return 2;
        if ("Pendiente Comprobante".equalsIgnoreCase(estado)) return 3;
        return 4;
    }
}
