package com.bms.reserva_servicio_backend.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CabanaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    @NotNull(message = "El precio por unidad es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precioPorUnidad;

    @NotBlank(message = "El estado es obligatorio")
    private String estado; // DISPONIBLE, MANTENIMIENTO, FUERA_SERVICIO

    @NotNull(message = "La capacidad de personas es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    private Integer capacidadPersonas;

    @NotNull(message = "El número de habitaciones es obligatorio")
    @Min(value = 1, message = "Debe tener al menos 1 habitación")
    private Integer numeroHabitaciones;

    @NotNull(message = "El número de baños es obligatorio")
    @Min(value = 1, message = "Debe tener al menos 1 baño")
    private Integer numeroBanos;

    @NotNull(message = "Los metros cuadrados son obligatorios")
    @DecimalMin(value = "0.0", inclusive = false, message = "Los metros cuadrados deben ser mayor a 0")
    private Double metrosCuadrados;

    @NotBlank(message = "El tipo de cabaña es obligatorio")
    private String tipoCabana; // ECONOMICA, STANDARD, PREMIUM, DELUXE

    @Size(max = 2000, message = "Los servicios incluidos no pueden exceder 2000 caracteres")
    private String serviciosIncluidos; // WiFi, TV Cable, Cocina equipada, etc.

}
