package com.bms.reserva_servicio_backend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RESPONSE de disponibilidad de item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadItemResponse {
    
    private Long itemId;
    private String nombreItem;
    private Integer cantidadTotal;
    private Integer cantidadDisponible;
    private Boolean disponible;
}