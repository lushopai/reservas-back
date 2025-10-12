package com.bms.reserva_servicio_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para items que se agregan a una reserva
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemReservaDTO {
    
    private Long itemId;
    private Integer cantidad;
    
    // Validación
    public boolean esValido() {
        return itemId != null && cantidad != null && cantidad > 0;
    }
}