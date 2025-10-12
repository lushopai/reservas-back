package com.bms.reserva_servicio_backend.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemReservadoResponse {
    
    private Long id;
    private String nombreItem;
    private String categoria;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}