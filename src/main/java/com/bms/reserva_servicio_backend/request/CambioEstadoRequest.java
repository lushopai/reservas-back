package com.bms.reserva_servicio_backend.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request para cambiar el estado de una reserva
 */
@Data
public class CambioEstadoRequest {

    @NotBlank(message = "El nuevo estado es obligatorio")
    private String nuevoEstado; // PENDIENTE, CONFIRMADA, EN_CURSO, COMPLETADA, CANCELADA

    private String motivo; // Opcional: razón del cambio (especialmente para cancelaciones)

    private String observaciones; // Notas adicionales del admin
}
