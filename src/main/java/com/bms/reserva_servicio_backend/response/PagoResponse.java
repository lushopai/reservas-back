package com.bms.reserva_servicio_backend.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoResponse {

    private Long id;
    private Long reservaId;
    private Long paqueteId;
    private BigDecimal monto;
    private String metodoPago;
    private String estado;
    private String transaccionId;
    private LocalDateTime fechaPago;

    // Información adicional de la reserva/paquete
    private String nombreRecurso;
    private String tipoReserva;
}
