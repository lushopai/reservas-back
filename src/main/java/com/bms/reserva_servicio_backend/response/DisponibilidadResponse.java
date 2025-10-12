package com.bms.reserva_servicio_backend.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RESPONSE de disponibilidad
 */
@Data
@Builder 
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadResponse {
    
    private Boolean disponible;
    private String mensaje;
    private List<String> motivosNoDisponible;
    
    // Constructor simple
    public DisponibilidadResponse(Boolean disponible) {
        this.disponible = disponible;
        this.mensaje = disponible ? "Disponible" : "No disponible";
    }
}