package com.bms.reserva_servicio_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bms.reserva_servicio_backend.models.Cabana;

public interface CabanaRepository extends JpaRepository<Cabana, Long> {
    List<Cabana> findByEstado(String estado);

    @Query("SELECT c FROM Cabana c WHERE c.capacidadPersonas >= :capacidad " +
            "AND c.estado = 'DISPONIBLE'")
    List<Cabana> findDisponiblesPorCapacidad(@Param("capacidad") Integer capacidad);

    @Query("SELECT c FROM Cabana c WHERE c.id NOT IN " +
            "(SELECT DISTINCT r.recurso.id FROM Reserva r " +
            "WHERE r.estado IN ('CONFIRMADA', 'EN_CURSO') " +
            "AND ((r.fechaInicio BETWEEN :inicio AND :fin) " +
            "OR (r.fechaFin BETWEEN :inicio AND :fin)))")
    List<Cabana> findCabanasDisponiblesEnPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}
