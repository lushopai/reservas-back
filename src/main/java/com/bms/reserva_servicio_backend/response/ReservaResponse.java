package com.bms.reserva_servicio_backend.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.List;

/**
 * RESPONSE de una reserva individual
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservaResponse {
    
    private Long id;
    private String tipoReserva;        // CABAÑA_DIA, SERVICIO_BLOQUE
    private String estado;             // PENDIENTE, CONFIRMADA, CANCELADA
    
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    
    // Precios
    private BigDecimal precioBase;
    private BigDecimal precioItems;
    private BigDecimal precioTotal;

    // Información del usuario
    private String nombreUsuario;
    private String emailUsuario;
    private String nombreRecurso;

    // Relaciones
    private RecursoResponse recurso;
    private List<ItemReservadoResponse> itemsReservados;
    
    // Info del paquete (si pertenece a uno)
    private Long paqueteId;
    private String nombrePaquete;
    private String estadoPaquete;  // Estado del paquete (BORRADOR, PENDIENTE, CONFIRMADO)
    // Precios del paquete completo
    private BigDecimal precioTotalPaquete;  // Suma de todas las reservas
    private BigDecimal descuentoPaquete;     // Descuento aplicado
    private BigDecimal precioFinalPaquete;   // Total con descuento
    // Lista de todas las reservas del paquete (para mostrar desglose completo)
    private List<ReservaResumenDTO> reservasPaquete;

    private String observaciones;

    // DTO simplificado para mostrar reservas del paquete
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservaResumenDTO {
        private Long id;
        private String nombreRecurso;
        private String tipoReserva;
        private BigDecimal precioBase;
        private BigDecimal precioItems;
        private BigDecimal precioTotal;
        private Integer cantidadItems;
    }
}