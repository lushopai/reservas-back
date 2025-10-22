package com.bms.reserva_servicio_backend.request;

import java.time.LocalDate;
import java.util.List;

import com.bms.reserva_servicio_backend.dto.ItemReservaDTO;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservaCabanaRequest {
    @NotNull(message = "El ID de la cabaña es obligatorio")
    private Long cabanaId;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o futura")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @FutureOrPresent(message = "La fecha de fin debe ser hoy o futura")
    private LocalDate fechaFin;

    // Items adicionales opcionales (vino, flores, etc.)
    private List<ItemReservaDTO> itemsAdicionales;

    private String observaciones;

}
