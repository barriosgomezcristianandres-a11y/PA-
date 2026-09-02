package com.isivi.app.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

/**
 * Helper reutilizable para el manejo, conversión y validación de horarios
 * en la zona horaria oficial del salón: America/Bogota.
 */
public final class HorarioUtil {

    public static final ZoneId ZONA_BOGOTA = ZoneId.of("America/Bogota");

    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("[hh:mm a][h:mm a][HH:mm][H:mm]")
            .toFormatter(Locale.ENGLISH);

    private HorarioUtil() {
        // Clase de utilidades estáticas
    }

    /**
     * Convierte un horario en formato de texto (ej. "08:00 AM", "12:00 PM", "01:30 PM", "12:00 AM", "06:00 PM")
     * a {@link LocalTime} reconociendo adecuadamente períodos AM/PM y 12/24h.
     */
    public static LocalTime parseTimeSlot(String horario) {
        if (horario == null || horario.isBlank()) {
            return null;
        }
        return LocalTime.parse(horario.trim().toUpperCase(Locale.ENGLISH), TIME_FORMATTER);
    }

    /**
     * Determina si una fecha y horario ya pasaron en base al Clock suministrado
     * (o por defecto en America/Bogota).
     *
     * Reglas:
     * A. Fecha pasada (anterior a hoy) -> true (no disponible)
     * B. Fecha actual (hoy):
     *    - Horarios anteriores a la hora actual -> true (no disponible)
     *    - Horario exactamente igual a la hora actual -> true (no disponible)
     *    - Horarios posteriores a la hora actual -> false (disponible)
     * C. Fecha futura -> false (disponible, sujeto a ocupación/bloqueo existente)
     */
    public static boolean isPastTimeSlot(LocalDate fecha, String horario, Clock clock) {
        if (fecha == null || horario == null || horario.isBlank()) {
            return false;
        }

        Clock effectiveClock = clock != null ? clock : Clock.system(ZONA_BOGOTA);
        LocalDate hoy = LocalDate.now(effectiveClock);

        // A. Fecha pasada
        if (fecha.isBefore(hoy)) {
            return true;
        }

        // C. Fecha futura
        if (fecha.isAfter(hoy)) {
            return false;
        }

        // B. Fecha actual (hoy)
        LocalTime slotTime = parseTimeSlot(horario);
        if (slotTime == null) {
            return false;
        }

        LocalTime horaActual = LocalTime.now(effectiveClock);

        // Si el horario es menor o igual a la hora actual, ya pasó
        return !slotTime.isAfter(horaActual);
    }

    /**
     * Extrae la duración en minutos de un texto (ej: "60 min", "90 minutos", "1h", "1h 30min", "2 horas", "45").
     * Si no se especifica o no es válida, retorna 60 minutos por defecto.
     */
    public static int parseDurationMinutes(String duracion) {
        if (duracion == null || duracion.isBlank()) {
            return 60;
        }
        String clean = duracion.trim().toLowerCase(Locale.ROOT);

        // Caso 1: "1h 30min", "2 horas 15 min", "1.5h"
        java.util.regex.Matcher mHours = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*h(?:oras?)?(?:\\s*(\\d+)\\s*(?:m|min|minutos)?)?").matcher(clean);
        if (mHours.find()) {
            double hours = Double.parseDouble(mHours.group(1));
            int mins = (int) Math.round(hours * 60);
            if (mHours.group(2) != null && !mHours.group(2).isBlank()) {
                mins += Integer.parseInt(mHours.group(2));
            }
            return mins > 0 ? mins : 60;
        }

        // Caso 2: "90 min", "45 minutos", "45"
        java.util.regex.Matcher mMins = java.util.regex.Pattern.compile("(\\d+)\\s*(?:m|min|minutos)?").matcher(clean);
        if (mMins.find()) {
            int mins = Integer.parseInt(mMins.group(1));
            return mins > 0 ? mins : 60;
        }
        return 60;
    }


    /**
     * Calcula la hora de finalización de una cita sumando los minutos de duración al horario de inicio.
     */
    public static LocalTime calculateEndTime(String horaInicio, int duracionMinutos) {
        LocalTime inicio = parseTimeSlot(horaInicio);
        if (inicio == null) {
            return null;
        }
        return inicio.plusMinutes(duracionMinutos > 0 ? duracionMinutos : 60);
    }

    /**
     * Determina si una cita ya finalizó por completo en base a su fecha, hora de inicio y duración.
     */
    public static boolean isAppointmentCompleted(LocalDate fecha, String horaInicio, int duracionMinutos, Clock clock) {
        if (fecha == null || horaInicio == null || horaInicio.isBlank()) {
            return false;
        }
        Clock effectiveClock = clock != null ? clock : Clock.system(ZONA_BOGOTA);
        LocalDate hoy = LocalDate.now(effectiveClock);

        if (fecha.isBefore(hoy)) {
            return true;
        }
        if (fecha.isAfter(hoy)) {
            return false;
        }

        LocalTime inicio = parseTimeSlot(horaInicio);
        if (inicio == null) {
            return isPastTimeSlot(fecha, horaInicio, effectiveClock);
        }

        java.time.LocalDateTime inicioDateTime = java.time.LocalDateTime.of(fecha, inicio);
        java.time.LocalDateTime finDateTime = inicioDateTime.plusMinutes(duracionMinutos > 0 ? duracionMinutos : 60);
        java.time.LocalDateTime ahoraDateTime = java.time.LocalDateTime.now(effectiveClock);
        return !ahoraDateTime.isBefore(finDateTime);
    }

    /**
     * Determina si una cita está en curso actualmente (horaInicio <= ahora < fin).
     */
    public static boolean isAppointmentInProgress(LocalDate fecha, String horaInicio, int duracionMinutos, Clock clock) {
        if (fecha == null || horaInicio == null || horaInicio.isBlank()) {
            return false;
        }
        Clock effectiveClock = clock != null ? clock : Clock.system(ZONA_BOGOTA);
        LocalDate hoy = LocalDate.now(effectiveClock);

        if (!fecha.equals(hoy)) {
            return false;
        }

        LocalTime inicio = parseTimeSlot(horaInicio);
        if (inicio == null) {
            return false;
        }

        java.time.LocalDateTime inicioDateTime = java.time.LocalDateTime.of(fecha, inicio);
        java.time.LocalDateTime finDateTime = inicioDateTime.plusMinutes(duracionMinutos > 0 ? duracionMinutos : 60);
        java.time.LocalDateTime ahoraDateTime = java.time.LocalDateTime.now(effectiveClock);
        return !ahoraDateTime.isBefore(inicioDateTime) && ahoraDateTime.isBefore(finDateTime);
    }
}

