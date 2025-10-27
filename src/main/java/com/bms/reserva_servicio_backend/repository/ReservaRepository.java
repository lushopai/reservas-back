package com.bms.reserva_servicio_backend.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bms.reserva_servicio_backend.enums.EstadoReserva;
import com.bms.reserva_servicio_backend.enums.TipoReserva;
import com.bms.reserva_servicio_backend.models.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
        List<Reserva> findByUserId(Long userId);

        List<Reserva> findByRecursoId(Long recursoId);

        List<Reserva> findByEstado(EstadoReserva estado);

        @Query("SELECT r FROM Reserva r WHERE r.recurso.id = :recursoId " +
                        "AND r.estado IN (com.bms.reserva_servicio_backend.enums.EstadoReserva.CONFIRMADA, com.bms.reserva_servicio_backend.enums.EstadoReserva.EN_CURSO) " +
                        "AND ((r.fechaInicio BETWEEN :inicio AND :fin) " +
                        "OR (r.fechaFin BETWEEN :inicio AND :fin) " +
                        "OR (r.fechaInicio <= :inicio AND r.fechaFin >= :fin))")
        List<Reserva> findReservasEnConflicto(
                        @Param("recursoId") Long recursoId,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);

        @Query("SELECT r FROM Reserva r WHERE r.user.id = :userId " +
                        "AND r.estado = :estado " +
                        "ORDER BY r.fechaInicio DESC")
        List<Reserva> findByUserIdAndEstado(
                        @Param("userId") Long userId,
                        @Param("estado") EstadoReserva estado);

        @Query("SELECT r FROM Reserva r WHERE DATE(r.fechaInicio) = :fecha " +
                        "AND r.estado = :estado")
        List<Reserva> findByFechaInicioAndEstado(
                        @Param("fecha") LocalDate fecha,
                        @Param("estado") EstadoReserva estado);

        @Query("SELECT r FROM Reserva r WHERE r.fechaReserva BETWEEN :inicio AND :fin")
        List<Reserva> findByFechaReservaBetween(
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);

        @Query("SELECT r FROM Reserva r WHERE r.tipoReserva = :tipo " +
                        "AND r.fechaInicio BETWEEN :inicio AND :fin")
        List<Reserva> findByTipoReservaAndFechaBetween(
                        @Param("tipo") TipoReserva tipo,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);

        @Query("SELECT r FROM Reserva r WHERE r.fechaInicio BETWEEN :inicio AND :fin")
        List<Reserva> findByFechaInicioBetween(
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);

        // Métodos para el scheduler de reservas
        @Query("SELECT r FROM Reserva r WHERE r.estado IN :estados AND r.fechaFin < :fechaFin")
        List<Reserva> findByEstadoInAndFechaFinBefore(
                        @Param("estados") List<EstadoReserva> estados,
                        @Param("fechaFin") LocalDateTime fechaFin);

        @Query("SELECT r FROM Reserva r WHERE r.estado = :estado AND r.fechaInicio < :fechaInicio")
        List<Reserva> findByEstadoAndFechaInicioBefore(
                        @Param("estado") EstadoReserva estado,
                        @Param("fechaInicio") LocalDateTime fechaInicio);

}
