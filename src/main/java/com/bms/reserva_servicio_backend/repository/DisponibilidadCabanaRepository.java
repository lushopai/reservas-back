package com.bms.reserva_servicio_backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bms.reserva_servicio_backend.models.DisponibilidadCabana;

public interface DisponibilidadCabanaRepository extends JpaRepository<DisponibilidadCabana, Long> {

    @Query("SELECT d FROM DisponibilidadCabana d WHERE d.cabana.id = :cabanaId " +
            "AND d.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<DisponibilidadCabana> findByRangoFechas(
            @Param("cabanaId") Long cabanaId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    Optional<DisponibilidadCabana> findByCabanaIdAndFecha(Long cabanaId, LocalDate fecha);
}
