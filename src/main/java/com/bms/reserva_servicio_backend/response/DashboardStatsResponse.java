package com.bms.reserva_servicio_backend.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    // Estadísticas de usuarios
    private Long totalUsuarios;
    private Long usuariosActivos;
    private Long usuariosNuevos; // Último mes

    // Estadísticas de reservas
    private Long totalReservas;
    private Long reservasPendientes;
    private Long reservasConfirmadas;
    private Long reservasCanceladas;
    private Long reservasCompletadas;
    private Long reservasEnCurso;

    // Estadísticas de recursos
    private Long totalCabanas;
    private Long cabanasDisponibles;
    private Long totalServicios;

    // Estadísticas de ingresos
    private BigDecimal ingresosMes;
    private BigDecimal ingresosHoy;
    private BigDecimal ingresosTotales;

    // Datos para gráficos
    private List<ReservasPorDiaDTO> reservasPorDia; // Últimos 7 días
    private List<IngresosPorMesDTO> ingresosPorMes; // Últimos 6 meses

    // Reservas recientes
    private List<ReservaResumeResponse> reservasRecientes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservasPorDiaDTO {
        private String fecha; // "2025-10-15"
        private Long cantidad;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngresosPorMesDTO {
        private String mes; // "2025-10"
        private BigDecimal monto;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservaResumeResponse {
        private Long id;
        private String nombreUsuario;
        private String emailUsuario;
        private String nombreRecurso;
        private String fechaReserva;
        private String estado;
        private BigDecimal precioTotal;
    }
}
