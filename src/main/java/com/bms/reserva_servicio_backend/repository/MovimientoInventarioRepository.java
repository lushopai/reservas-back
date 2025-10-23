package com.bms.reserva_servicio_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bms.reserva_servicio_backend.models.MovimientoInventario;
import com.bms.reserva_servicio_backend.models.MovimientoInventario.TipoMovimiento;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    /**
     * Buscar movimientos por item
     */
    List<MovimientoInventario> findByItemIdOrderByFechaMovimientoDesc(Long itemId);

    /**
     * Buscar movimientos por tipo
     */
    List<MovimientoInventario> findByTipoMovimientoOrderByFechaMovimientoDesc(TipoMovimiento tipo);

    /**
     * Buscar movimientos por reserva
     */
    List<MovimientoInventario> findByReservaIdOrderByFechaMovimientoDesc(Long reservaId);

    /**
     * Buscar movimientos por usuario
     */
    List<MovimientoInventario> findByUsuarioIdOrderByFechaMovimientoDesc(Long usuarioId);

    /**
     * Buscar movimientos en un rango de fechas
     */
    @Query("SELECT m FROM MovimientoInventario m WHERE m.fechaMovimiento BETWEEN :inicio AND :fin ORDER BY m.fechaMovimiento DESC")
    List<MovimientoInventario> findByFechaMovimientoBetween(
        @Param("inicio") LocalDateTime inicio,
        @Param("fin") LocalDateTime fin
    );

    /**
     * Obtener los últimos N movimientos
     */
    List<MovimientoInventario> findTop50ByOrderByFechaMovimientoDesc();

    /**
     * Buscar movimientos por item y tipo
     */
    List<MovimientoInventario> findByItemIdAndTipoMovimientoOrderByFechaMovimientoDesc(
        Long itemId,
        TipoMovimiento tipo
    );

    /**
     * Contar movimientos por tipo
     */
    @Query("SELECT COUNT(m) FROM MovimientoInventario m WHERE m.tipoMovimiento = :tipo")
    Long countByTipoMovimiento(@Param("tipo") TipoMovimiento tipo);
}
