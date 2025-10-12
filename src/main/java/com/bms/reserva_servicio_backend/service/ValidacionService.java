package com.bms.reserva_servicio_backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bms.reserva_servicio_backend.dto.ServicioReservaDTO;

@Service
public class ValidacionService {

    // Configuraciones desde application.yml
    @Value("${app.reservas.anticipacion-minima-horas:24}")
    private int anticipacionMinimaHoras;

    @Value("${app.reservas.estadia-minima-dias:1}")
    private int estadiaMinimaDias;

    @Value("${app.reservas.estadia-maxima-dias:30}")
    private int estadiaMaximaDias;

    @Value("${app.reservas.servicio-anticipacion-minima-horas:2}")
    private int servicioAnticipacionMinimaHoras;

    /**
     * Validar reserva de cabaña
     */
    public void validarReservaCabana(LocalDate fechaInicio,
            LocalDate fechaFin,
            Integer capacidadRequerida) {

        // 1. Validar que las fechas no sean nulas
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }

        // 2. Validar que fecha fin > fecha inicio
        if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
            throw new IllegalArgumentException(
                    "La fecha de fin debe ser posterior a la fecha de inicio");
        }

        // 3. Validar que no se reserve en el pasado
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioReserva = fechaInicio.atStartOfDay();

        if (inicioReserva.isBefore(ahora)) {
            throw new IllegalArgumentException(
                    "No se puede reservar en una fecha pasada");
        }

        // 4. Validar anticipación mínima
        if (inicioReserva.isBefore(ahora.plusHours(anticipacionMinimaHoras))) {
            throw new IllegalArgumentException(
                    String.format("Debe reservar con al menos %d horas de anticipación",
                            anticipacionMinimaHoras));
        }

        // 5. Validar estadía mínima
        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        if (dias < estadiaMinimaDias) {
            throw new IllegalArgumentException(
                    String.format("La estadía mínima es de %d día(s)", estadiaMinimaDias));
        }

        // 6. Validar estadía máxima
        if (dias > estadiaMaximaDias) {
            throw new IllegalArgumentException(
                    String.format("La estadía máxima es de %d días", estadiaMaximaDias));
        }

        // 7. Validar capacidad si se proporciona
        if (capacidadRequerida != null && capacidadRequerida <= 0) {
            throw new IllegalArgumentException(
                    "La capacidad debe ser mayor a 0");
        }

        // 8. Validar que no reserve con más de 1 año de anticipación
        if (inicioReserva.isAfter(ahora.plusMonths(12))) {
            throw new IllegalArgumentException(
                    "No se pueden hacer reservas con más de 12 meses de anticipación");
        }
    }

    /**
     * Validar reserva de servicio
     */
    public void validarReservaServicio(LocalDate fecha,
            LocalTime horaInicio,
            Integer duracionBloques) {

        // 1. Validar que los datos no sean nulos
        if (fecha == null || horaInicio == null || duracionBloques == null) {
            throw new IllegalArgumentException("Los datos de la reserva no pueden ser nulos");
        }

        // 2. Validar que no sea en el pasado
        LocalDateTime inicioReserva = fecha.atTime(horaInicio);
        LocalDateTime ahora = LocalDateTime.now();

        if (inicioReserva.isBefore(ahora)) {
            throw new IllegalArgumentException(
                    "No se puede reservar en una fecha/hora pasada");
        }

        // 3. Validar anticipación mínima (ej: 2 horas)
        if (inicioReserva.isBefore(ahora.plusHours(servicioAnticipacionMinimaHoras))) {
            throw new IllegalArgumentException(
                    String.format("Debe reservar el servicio con al menos %d horas de anticipación",
                            servicioAnticipacionMinimaHoras));
        }

        // 4. Validar duración de bloques
        if (duracionBloques < 1) {
            throw new IllegalArgumentException(
                    "La duración debe ser de al menos 1 bloque");
        }

        if (duracionBloques > 8) {
            throw new IllegalArgumentException(
                    "La duración máxima es de 8 bloques horarios");
        }

        // 5. Validar horario de operación (ejemplo: 8:00 - 22:00)
        LocalTime horaApertura = LocalTime.of(8, 0);
        LocalTime horaCierre = LocalTime.of(22, 0);

        if (horaInicio.isBefore(horaApertura)) {
            throw new IllegalArgumentException(
                    "El horario de inicio no puede ser antes de las " + horaApertura);
        }

        // Calcular hora de fin aproximada (asumiendo bloques de 1 hora)
        LocalTime horaFinAproximada = horaInicio.plusHours(duracionBloques);
        if (horaFinAproximada.isAfter(horaCierre)) {
            throw new IllegalArgumentException(
                    "El servicio terminaría después del horario de cierre (" + horaCierre + ")");
        }
    }

    /**
     * Validar datos de paquete
     */
    public void validarPaquete(LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Long cabanaId,
            List<ServicioReservaDTO> servicios) {

        // 1. Validar fechas
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas del paquete no pueden ser nulas");
        }

        if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
            throw new IllegalArgumentException(
                    "La fecha de fin debe ser posterior a la fecha de inicio");
        }

        // 2. Validar que incluya al menos un recurso
        if (cabanaId == null && (servicios == null || servicios.isEmpty())) {
            throw new IllegalArgumentException(
                    "El paquete debe incluir al menos una cabaña o un servicio");
        }

        // 3. Validar que los servicios estén dentro del rango del paquete
        if (servicios != null) {
            for (ServicioReservaDTO servicio : servicios) {
                LocalDateTime inicioServicio = servicio.getFecha().atTime(servicio.getHoraInicio());

                if (inicioServicio.isBefore(fechaInicio)) {
                    throw new IllegalArgumentException(
                            "Los servicios deben estar dentro del rango de fechas del paquete");
                }

                if (inicioServicio.isAfter(fechaFin)) {
                    throw new IllegalArgumentException(
                            "Los servicios deben estar dentro del rango de fechas del paquete");
                }
            }
        }

        // 4. Validar anticipación
        if (fechaInicio.isBefore(LocalDateTime.now().plusHours(anticipacionMinimaHoras))) {
            throw new IllegalArgumentException(
                    String.format("Debe reservar con al menos %d horas de anticipación",
                            anticipacionMinimaHoras));
        }
    }

    /**
     * Validar datos de cliente
     */
    public void validarCliente(String nombre, String email, String telefono, String documento) {

        // 1. Validar nombre
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (nombre.length() < 2 || nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre debe tener entre 2 y 100 caracteres");
        }

        // 2. Validar email
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("El email no es válido");
        }

        // 3. Validar teléfono
        if (telefono == null || !telefono.matches("^\\+?[0-9]{9,15}$")) {
            throw new IllegalArgumentException("El teléfono no es válido");
        }

        // 4. Validar documento (RUT chileno simplificado)
        if (documento == null || documento.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }
    }

    /**
     * Validar monto de pago
     */
    public void validarPago(BigDecimal monto, BigDecimal montoEsperado) {

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }

        if (montoEsperado != null && monto.compareTo(montoEsperado) != 0) {
            throw new IllegalArgumentException(
                    String.format("El monto no coincide. Esperado: %s, Recibido: %s",
                            montoEsperado, monto));
        }
    }

    /**
     * Validar capacidad de cabaña
     */
    public void validarCapacidad(Integer capacidadCabana, Integer personasRequeridas) {

        if (personasRequeridas == null || personasRequeridas <= 0) {
            throw new IllegalArgumentException("Debe indicar el número de personas");
        }

        if (capacidadCabana == null || capacidadCabana <= 0) {
            throw new IllegalArgumentException("La cabaña no tiene capacidad definida");
        }

        if (personasRequeridas > capacidadCabana) {
            throw new IllegalArgumentException(
                    String.format("La cabaña tiene capacidad para %d personas, " +
                            "pero se solicitó para %d", capacidadCabana, personasRequeridas));
        }
    }

    /**
     * Validar estado de recurso
     */
    public void validarEstadoRecurso(String estado) {

        if (estado == null) {
            throw new IllegalArgumentException("El estado del recurso no puede ser nulo");
        }

        if ("FUERA_SERVICIO".equals(estado)) {
            throw new IllegalArgumentException("El recurso está fuera de servicio");
        }

        if ("MANTENIMIENTO".equals(estado)) {
            throw new IllegalArgumentException("El recurso está en mantenimiento");
        }
    }

}
