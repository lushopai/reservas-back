package com.bms.reserva_servicio_backend.request;

import java.math.BigDecimal;

import com.bms.reserva_servicio_backend.dto.PagoDTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PagoRequest {
    
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;
    
    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(regexp = "EFECTIVO|TARJETA|TRANSFERENCIA|WEBPAY", 
             message = "Método de pago inválido")
    private String metodoPago;
    
    // Opcional: ID de transacción externa (de pasarela de pago)
    private String transaccionId;
    
    // Opcional: Datos adicionales según el método
    private DatosPagoAdicionales datosAdicionales;
    
    /**
     * Convertir Request a DTO interno
     */
    public PagoDTO toDTO() {
        PagoDTO dto = new PagoDTO();
        dto.setMonto(this.monto);
        dto.setMetodoPago(this.metodoPago);
        dto.setTransaccionId(this.transaccionId);
        return dto;
    }
}
