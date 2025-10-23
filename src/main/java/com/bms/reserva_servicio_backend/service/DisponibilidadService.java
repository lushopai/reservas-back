package com.bms.reserva_servicio_backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bms.reserva_servicio_backend.models.BloqueHorario;
import com.bms.reserva_servicio_backend.models.Cabana;
import com.bms.reserva_servicio_backend.models.DisponibilidadCabana;
import com.bms.reserva_servicio_backend.models.Reserva;
import com.bms.reserva_servicio_backend.models.ServicioEntretencion;
import com.bms.reserva_servicio_backend.repository.BloqueHorarioRepository;
import com.bms.reserva_servicio_backend.repository.CabanaRepository;
import com.bms.reserva_servicio_backend.repository.DisponibilidadCabanaRepository;
import com.bms.reserva_servicio_backend.repository.ReservaRepository;
import com.bms.reserva_servicio_backend.repository.ServicioEntretencionRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class DisponibilidadService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private BloqueHorarioRepository bloqueHorarioRepository;
    @Autowired
    private DisponibilidadCabanaRepository disponibilidadCabanaRepository;

    @Autowired
    private CabanaRepository cabanaRepository;

    @Autowired
    private ServicioEntretencionRepository servicioRepository;

    public boolean validarDisponibilidadCabana(Long cabanaId, LocalDate fechaInicio, LocalDate fechaFin) {
        Cabana cabana = cabanaRepository.findById(cabanaId).orElseThrow(
                () -> new EntityNotFoundException("Cabaña no encontrada"));

        if ("FUERA_SERVICIO".equals(cabana.getEstado())) {
            return false;
        }
        // Verificar bloqueos manuales en la tabla de disponibilidad
        List<DisponibilidadCabana> bloqueos = disponibilidadCabanaRepository.findByRangoFechas(cabanaId, fechaInicio,
                fechaFin);

        boolean hayBloqueos = bloqueos.stream()
                .anyMatch(d -> !d.getDisponible());

        if (hayBloqueos) {
            return false;
        }

        // Verificar reservas existentes
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59);

        List<Reserva> conflictos = reservaRepository
                .findReservasEnConflicto(cabanaId, inicio, fin);

        return conflictos.isEmpty();
    }

    /**
     * Validar disponibidad de servicio en un bloque horario
     */

    public boolean validarDisponibilidadServicio(Long servicioId,
            LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        // Verificar que el servicio exista
        ServicioEntretencion servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));

        if ("FUERA_SERVICIO".equals(servicio.getEstado())) {
            return false;

        }
        // Verificar bloques horarios
        List<BloqueHorario> bloquesEnRango = bloqueHorarioRepository
                .findBloquesEnRango(servicioId, fecha, horaInicio, horaFin);

        // Si hay bloques no disponibles no se puede reservar

        boolean hayBloquesNoDisponibles = bloquesEnRango.stream()
                .anyMatch(b -> !b.getDisponible());

        if (hayBloquesNoDisponibles) {
            return false;
        }

        // Verificar reservas existentes
        LocalDateTime inicio = fecha.atTime(horaInicio);
        LocalDateTime fin = fecha.atTime(horaFin);

        List<Reserva> conflictos = reservaRepository
                .findReservasEnConflicto(servicioId, inicio, fin);

        return conflictos.isEmpty();
    }

    /**
     * Obtener bloques horarios disponibles de un servicio en una fecha
     */
    public List<BloqueHorario> obtenerBloquesDisponibles(Long servicioId, LocalDate fecha) {
        return bloqueHorarioRepository.findBloquesDisponibles(servicioId, fecha);
    }

    /**
     * Bloquear fechas de cabaña despues de confirmar reserva
     */

    public void bloquearFechasCabana(Long cabanaId, LocalDate fechaInicio,
            LocalDate fechaFin) {
        LocalDate fechaActual = fechaInicio;

        while (!fechaActual.isAfter(fechaFin)) {
            final LocalDate fecha = fechaActual;

            DisponibilidadCabana disponibilidad = disponibilidadCabanaRepository
                    .findByCabanaIdAndFecha(cabanaId, fecha)
                    .orElseGet(() -> {
                        DisponibilidadCabana nueva = new DisponibilidadCabana();
                        nueva.setCabana(cabanaRepository.findById(cabanaId).orElseThrow());
                        nueva.setFecha(fecha);
                        return nueva;
                    });

            disponibilidad.setDisponible(false);
            disponibilidad.setMotivoNoDisponible("RESERVADA");
            disponibilidadCabanaRepository.save(disponibilidad);

            fechaActual = fechaActual.plusDays(1);

        }

    }

    /**
     * Bloquear bloque horario de servicio
     */
    public void bloquearBloqueHorario(Long servicioId, LocalDate fecha,
            LocalTime horarioInicio, LocalTime horaFin) {

        List<BloqueHorario> bloques = bloqueHorarioRepository
                .findBloquesEnRango(servicioId, fecha, horarioInicio, horaFin);

        for (BloqueHorario bloque : bloques) {
            bloque.setDisponible(false);
            bloque.setMotivoNoDisponible("RESERVADO");
            bloqueHorarioRepository.save(bloque);
        }
    }

    /**
     * Liberar recurso cuando se cancela una reserva
     */
    public void liberarRecurso(Reserva reserva) {
        if ("CABAÑA_DIA".equals(reserva.getTipoReserva())) {
            liberarFechasCabana(
                    reserva.getRecurso().getId(),
                    reserva.getFechaInicio().toLocalDate(),
                    reserva.getFechaFin().toLocalDate());
        } else if ("SERVICIO_BLOQUE".equals(reserva.getTipoReserva())) {
            liberarBloqueHorario(
                    reserva.getRecurso().getId(),
                    reserva.getFechaInicio().toLocalDate(),
                    reserva.getFechaInicio().toLocalTime(),
                    reserva.getFechaFin().toLocalTime());
        }
    }

    private void liberarFechasCabana(Long cabanaId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<DisponibilidadCabana> disponibilidades = disponibilidadCabanaRepository
                .findByRangoFechas(cabanaId, fechaInicio, fechaFin);

        for (DisponibilidadCabana disp : disponibilidades) {
            if ("RESERVADA".equals(disp.getMotivoNoDisponible())) {
                disp.setDisponible(true);
                disp.setMotivoNoDisponible(null);
                disponibilidadCabanaRepository.save(disp);
            }
        }
    }

    private void liberarBloqueHorario(Long servicioId, LocalDate fecha,
            LocalTime horaInicio, LocalTime horaFin) {
        List<BloqueHorario> bloques = bloqueHorarioRepository
                .findBloquesEnRango(servicioId, fecha, horaInicio, horaFin);

        for (BloqueHorario bloque : bloques) {
            if ("RESERVADO".equals(bloque.getMotivoNoDisponible())) {
                bloque.setDisponible(true);
                bloque.setMotivoNoDisponible(null);
                bloqueHorarioRepository.save(bloque);
            }
        }
    }

    /**
     * Generar bloques horarios para un servicio (útil para inicialización)
     */
    public void generarBloquesHorarios(Long servicioId, LocalDate fecha,
            LocalTime horaApertura, LocalTime horaCierre,
            Integer duracionBloqueMinutos) {
        ServicioEntretencion servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));

        LocalTime horaActual = horaApertura;

        while (horaActual.isBefore(horaCierre)) {
            LocalTime horaFinBloque = horaActual.plusMinutes(duracionBloqueMinutos);

            if (horaFinBloque.isAfter(horaCierre)) {
                break;
            }

            // Verificar si el bloque ya existe para evitar duplicados
            boolean existe = bloqueHorarioRepository.existeBloqueExacto(
                    servicioId, fecha, horaActual, horaFinBloque);

            if (!existe) {
                BloqueHorario bloque = new BloqueHorario();
                bloque.setServicio(servicio);
                bloque.setFecha(fecha);
                bloque.setHoraInicio(horaActual);
                bloque.setHoraFin(horaFinBloque);
                bloque.setDisponible(true);

                bloqueHorarioRepository.save(bloque);
            }

            horaActual = horaFinBloque;
        }
    }

    /**
     * Obtener todas las fechas reservadas de una cabaña en un rango
     * (útil para bloquear visualmente en el calendario)
     */
    public List<LocalDate> obtenerFechasReservadas(Long cabanaId, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59);

        // Obtener todas las reservas activas (no canceladas) de la cabaña en el rango
        List<Reserva> reservas = reservaRepository
                .findReservasEnConflicto(cabanaId, inicio, fin);

        List<LocalDate> fechasReservadas = new ArrayList<>();

        for (Reserva reserva : reservas) {
            // Solo incluir reservas confirmadas o pendientes (no canceladas)
            if (!"CANCELADA".equals(reserva.getEstado())) {
                LocalDate fechaActual = reserva.getFechaInicio().toLocalDate();
                LocalDate fechaFinReserva = reserva.getFechaFin().toLocalDate();

                // Agregar todas las fechas del rango de la reserva
                while (!fechaActual.isAfter(fechaFinReserva)) {
                    fechasReservadas.add(fechaActual);
                    fechaActual = fechaActual.plusDays(1);
                }
            }
        }

        return fechasReservadas.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Obtener bloques horarios por rango de fechas
     */
    public List<BloqueHorario> obtenerBloquesPorRangoFechas(Long servicioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return bloqueHorarioRepository.findBloquesPorRangoFechas(servicioId, fechaInicio, fechaFin);
    }

    /**
     * Desbloquear un bloque horario específico
     */
    public void desbloquearBloque(Long bloqueId) {
        BloqueHorario bloque = bloqueHorarioRepository.findById(bloqueId)
            .orElseThrow(() -> new EntityNotFoundException("Bloque no encontrado"));

        // Solo desbloquear si no está reservado
        if (!"RESERVADO".equals(bloque.getMotivoNoDisponible())) {
            bloque.setDisponible(true);
            bloque.setMotivoNoDisponible(null);
            bloqueHorarioRepository.save(bloque);
        }
    }
}
