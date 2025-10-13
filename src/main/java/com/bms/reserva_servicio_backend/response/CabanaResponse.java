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
public class CabanaResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precioPorUnidad;
    private String estado; // DISPONIBLE, MANTENIMIENTO, FUERA_SERVICIO

    // Características específicas de cabaña
    private Integer capacidadPersonas;
    private Integer numeroHabitaciones;
    private Integer numeroBanos;
    private Double metrosCuadrados;
    private String tipoCabana; // ECONOMICA, STANDARD, PREMIUM, DELUXE
    private String serviciosIncluidos;

    // Información adicional
    private Integer totalReservas; // Total de reservas históricas
    private Boolean disponibleHoy; // Si está disponible hoy
    private Integer itemsInventario; // Cantidad de items asociados

}
