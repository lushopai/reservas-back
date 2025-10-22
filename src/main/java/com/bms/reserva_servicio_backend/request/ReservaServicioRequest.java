package com.bms.reserva_servicio_backend.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.bms.reserva_servicio_backend.dto.ItemReservaDTO;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * REQUEST para reservar un servicio de entretenimiento
 * POST /api/reservas/servicio
 */
@Data
public class ReservaServicioRequest {

    @NotNull(message = "El ID del servicio es obligatorio")
    private Long servicioId;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha debe ser hoy o futura")
    private LocalDate fecha;
    
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;
    
    @NotNull(message = "La duración en bloques es obligatoria")
    @Min(value = 1, message = "Debe reservar al menos 1 bloque")
    @Max(value = 8, message = "No puede reservar más de 8 bloques")
    private Integer duracionBloques;
    
    // Equipamiento adicional (raquetas, pelotas, etc.)
    private List<ItemReservaDTO> equipamiento;
    
    private String observaciones;

}
