package com.bms.reserva_servicio_backend.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RESPONSE de item de inventario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemInventarioResponse {

    private Long id;
    private String nombre;
    private String categoria;
    private Integer cantidadTotal;
    private Integer cantidadDisponible;
    private String estadoItem;
    private Boolean esReservable;
    private BigDecimal precioReserva;

    // Recurso al que pertenece
    private Long recursoId;
    private String nombreRecurso;
}