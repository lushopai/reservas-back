package com.bms.reserva_servicio_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bms.reserva_servicio_backend.response.DashboardStatsResponse;
import com.bms.reserva_servicio_backend.response.SuccessResponse;
import com.bms.reserva_servicio_backend.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * GET /api/dashboard/stats
     * Obtener estadísticas del dashboard
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<DashboardStatsResponse>> obtenerEstadisticas() {

        DashboardStatsResponse stats = dashboardService.obtenerEstadisticas();

        return ResponseEntity.ok(
                SuccessResponse.of(stats, "Estadísticas obtenidas exitosamente"));
    }
}
