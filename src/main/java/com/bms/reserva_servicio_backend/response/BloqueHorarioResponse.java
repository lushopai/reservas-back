package com.bms.reserva_servicio_backend.response;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RESPONSE de bloques horarios disponibles
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloqueHorarioResponse {
    
    private Long id;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Boolean disponible;
    private String motivoNoDisponible;
}