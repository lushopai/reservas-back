package com.bms.reserva_servicio_backend.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadCabanaResponse {
    private Long id;
    private Long cabanaId;
    private String nombreCabana;
    private LocalDate fecha;
    private Boolean disponible;
    private String motivoNoDisponible;
    private BigDecimal precioEspecial;
}
