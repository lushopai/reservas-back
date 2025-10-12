package com.bms.reserva_servicio_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO interno para paquetes (usado entre servicios)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaqueteReservaDTO {
    
    private String nombre;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    
    private Long cabanaId;
    private List<ItemReservaDTO> itemsCabana;
    
    private List<ServicioReservaDTO> servicios;
    
    private String notasEspeciales;
}
