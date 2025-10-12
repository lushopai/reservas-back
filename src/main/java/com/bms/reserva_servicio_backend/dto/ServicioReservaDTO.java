package com.bms.reserva_servicio_backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para servicios dentro de un paquete
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicioReservaDTO {
    
    private Long servicioId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private Integer duracionBloques;
    private List<ItemReservaDTO> equipamiento;
}