package com.bms.reserva_servicio_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bms.reserva_servicio_backend.models.ItemsInventario;

public interface ItemInventarioRepository extends JpaRepository<ItemsInventario, Long> {
    
    List<ItemsInventario> findByRecursoId(Long recursoId);
    
    List<ItemsInventario> findByEsReservable(Boolean esReservable);
    
    @Query("SELECT i FROM ItemsInventario i WHERE i.categoria = :categoria " +
           "AND i.esReservable = true AND i.cantidadDisponible > 0")
    List<ItemsInventario> findDisponiblesPorCategoria(@Param("categoria") String categoria);
}