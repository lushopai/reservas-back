package com.bms.reserva_servicio_backend.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bms.reserva_servicio_backend.models.BloqueHorario;

public interface BloqueHorarioRepository extends JpaRepository<BloqueHorario, Long> {

    @Query("SELECT b FROM BloqueHorario b WHERE b.servicio.id = :servicioId " +
            "AND b.fecha = :fecha AND b.disponible = true " +
            "ORDER BY b.horaInicio")
    List<BloqueHorario> findBloquesDisponibles(
            @Param("servicioId") Long servicioId,
            @Param("fecha") LocalDate fecha);

    @Query("SELECT b FROM BloqueHorario b WHERE b.servicio.id = :servicioId " +
            "AND b.fecha = :fecha " +
            "AND ((b.horaInicio BETWEEN :horaInicio AND :horaFin) " +
            "OR (b.horaFin BETWEEN :horaInicio AND :horaFin) " +
            "OR (b.horaInicio <= :horaInicio AND b.horaFin >= :horaFin))")
    List<BloqueHorario> findBloquesEnRango(
            @Param("servicioId") Long servicioId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin);

    @Query("SELECT b FROM BloqueHorario b WHERE b.servicio.id = :servicioId " +
            "AND b.fecha BETWEEN :fechaInicio AND :fechaFin " +
            "ORDER BY b.fecha, b.horaInicio")
    List<BloqueHorario> findBloquesPorRangoFechas(
            @Param("servicioId") Long servicioId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT COUNT(b) > 0 FROM BloqueHorario b WHERE b.servicio.id = :servicioId " +
            "AND b.fecha = :fecha AND b.horaInicio = :horaInicio AND b.horaFin = :horaFin")
    boolean existeBloqueExacto(
            @Param("servicioId") Long servicioId,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin);
}