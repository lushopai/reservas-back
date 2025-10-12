package com.bms.reserva_servicio_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bms.reserva_servicio_backend.models.Pagos;

public interface PagoRepository extends JpaRepository<Pagos, Long> {

    List<Pagos> findByReservaId(Long reservaId);

    List<Pagos> findByPaqueteId(Long paqueteId);

    @Query("SELECT p FROM Pagos p WHERE p.estado = :estado " +
            "AND p.fechaPago BETWEEN :fechaInicio AND :fechaFin")
    List<Pagos> findPagosPorPeriodoYEstado(
            @Param("estado") String estado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
}