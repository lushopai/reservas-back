package com.bms.reserva_servicio_backend.request;

import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ServicioRequest {

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

    @NotBlank(message = "El tipo de servicio es obligatorio")
    private String tipoServicio; // CANCHA_TENIS, PISCINA, QUINCHO, SPA, GIMNASIO, SALA_JUEGOS, etc

    @NotNull(message = "La capacidad máxima es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    private Integer capacidadMaxima;

    @NotNull(message = "La duración del bloque es obligatoria")
    @Min(value = 15, message = "La duración mínima es 15 minutos")
    private Integer duracionBloqueMinutos; // 60 para 1 hora, 120 para 2 horas, etc

    private Boolean requiereSupervision = false; // Valor por defecto false si no se envía

    // Horarios de atención
    private LocalTime horaApertura; // Ej: 09:00 - opcional, puede ser null
    private LocalTime horaCierre;   // Ej: 18:00 - opcional, puede ser null

}
