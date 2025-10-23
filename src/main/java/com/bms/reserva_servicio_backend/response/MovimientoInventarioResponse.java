package com.bms.reserva_servicio_backend.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventarioResponse {

    private Long id;

    // Información del item
    private Long itemId;
    private String nombreItem;
    private String categoriaItem;

    // Información del movimiento
    private String tipoMovimiento;
    private Integer cantidad;
    private LocalDateTime fechaMovimiento;

    // Stock
    private Integer stockAnterior;
    private Integer stockPosterior;

    // Información de la reserva (si aplica)
    private Long reservaId;
    private String nombreRecurso;

    // Información del usuario
    private Long usuarioId;
    private String nombreUsuario;

    // Observaciones
    private String observaciones;
}
