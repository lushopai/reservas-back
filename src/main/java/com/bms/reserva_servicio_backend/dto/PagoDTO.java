package com.bms.reserva_servicio_backend.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO interno para pagos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoDTO {
    
    private BigDecimal monto;
    private String metodoPago;
    private String transaccionId;
}
