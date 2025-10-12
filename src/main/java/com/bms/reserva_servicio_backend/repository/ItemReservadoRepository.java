package com.bms.reserva_servicio_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bms.reserva_servicio_backend.models.ItemReservado;

public interface ItemReservadoRepository extends JpaRepository<ItemReservado, Long> {

        List<ItemReservado> findByReservaId(Long reservaId);

        @Query("SELECT COALESCE(SUM(ir.cantidad), 0) FROM ItemReservado ir " +
                        "WHERE ir.item.id = :itemId " +
                        "AND ir.reserva.estado IN ('CONFIRMADA', 'EN_CURSO') " +
                        "AND ((ir.reserva.fechaInicio BETWEEN :inicio AND :fin) " +
                        "OR (ir.reserva.fechaFin BETWEEN :inicio AND :fin) " +
                        "OR (ir.reserva.fechaInicio <= :inicio AND ir.reserva.fechaFin >= :fin))")
        Integer countReservadasEnPeriodo(
                        @Param("itemId") Long itemId,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);

        @Query("SELECT ir FROM ItemReservado ir WHERE ir.item.id = :itemId " +
                        "AND ir.reserva.estado = :estado")
        List<ItemReservado> findByItemIdAndReservaEstado(
                        @Param("itemId") Long itemId,
                        @Param("estado") String estado);
}
