package com.bms.reserva_servicio_backend.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RESPONSE de un recurso (cabaña o servicio)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecursoResponse {
    
    private Long id;
    private String tipo;               // CABAÑA, SERVICIO
    private String nombre;
    private String descripcion;
    private String estado;
    private BigDecimal precioPorUnidad;
    
    // Si es cabaña
    private Integer capacidadPersonas;
    private Integer numeroHabitaciones;
    private String tipoCabana;
    
    // Si es servicio
    private String tipoServicio;
    private Integer duracionBloqueMinutos;
}
