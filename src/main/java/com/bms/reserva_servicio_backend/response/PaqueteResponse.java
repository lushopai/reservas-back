package com.bms.reserva_servicio_backend.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RESPONSE de un paquete completo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaqueteResponse {
    
    private Long id;
    private String nombrePaquete;
    private String estado;             // BORRADOR, CONFIRMADO, EN_CURSO, COMPLETADO
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    
    // Precios
    private BigDecimal precioTotal;    // Suma sin descuento
    private BigDecimal descuento;      // Monto descontado
    private BigDecimal precioFinal;    // Total - descuento
    private Double porcentajeDescuento;
    
    // Relaciones
    private List<ReservaResponse> reservas;
    
    // Resumen
    private Integer cantidadReservas;
    private Integer diasEstadia;
    
    private String notasEspeciales;
    
    // Info de pago
    private String estadoPago;         // PENDIENTE, SEÑA_PAGADA, PAGADO_COMPLETO
    private BigDecimal montoPagado;
    private BigDecimal montoPendiente;
}