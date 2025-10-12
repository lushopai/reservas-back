package com.bms.reserva_servicio_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bms.reserva_servicio_backend.models.PaqueteReserva;

public interface PaqueteReservaRepository extends JpaRepository<PaqueteReserva, Long> {

        List<PaqueteReserva> findByUserId(Long userId);

        List<PaqueteReserva> findByEstado(String estado);

        @Query("SELECT p FROM PaqueteReserva p WHERE p.user.id = :userId " +
                        "AND p.estado = :estado")
        List<PaqueteReserva> findByClienteIdAndEstado(
                        @Param("userId") Long userId,
                        @Param("estado") String estado);

        @Query("SELECT p FROM PaqueteReserva p WHERE p.estado = :estado " +
                        "AND p.fechaCreacion < :fecha")
        List<PaqueteReserva> findByEstadoAndFechaCreacionBefore(
                        @Param("estado") String estado,
                        @Param("fecha") LocalDateTime fecha);
}