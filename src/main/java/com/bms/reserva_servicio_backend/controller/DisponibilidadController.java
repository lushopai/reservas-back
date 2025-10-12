package com.bms.reserva_servicio_backend.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bms.reserva_servicio_backend.models.BloqueHorario;
import com.bms.reserva_servicio_backend.response.BloqueHorarioResponse;
import com.bms.reserva_servicio_backend.response.DisponibilidadResponse;
import com.bms.reserva_servicio_backend.service.DisponibilidadService;

@RestController
@RequestMapping("/api/disponibilidad")
@CrossOrigin(origins = "*")
public class DisponibilidadController {
    
    @Autowired
    private DisponibilidadService disponibilidadService;
    
    /**
     * GET /api/disponibilidad/cabanas/{id}
     * Consultar si una cabaña está disponible
     */
    @GetMapping("/cabanas/{id}")
    public ResponseEntity<DisponibilidadResponse> consultarDisponibilidadCabana(
            @PathVariable Long id,
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {
        
        // El servicio valida:
        // - No hay reservas en conflicto
        // - No hay bloqueos manuales
        // - Estado del recurso
        boolean disponible = disponibilidadService.validarDisponibilidadCabana(
            id, fechaInicio, fechaFin
        );
        
        return ResponseEntity.ok(new DisponibilidadResponse(disponible));
    }
    
    /**
     * GET /api/disponibilidad/servicios/{id}
     * Obtener bloques horarios disponibles
     */
    @GetMapping("/servicios/{id}")
    public ResponseEntity<List<BloqueHorarioResponse>> consultarBloquesDisponibles(
            @PathVariable Long id,
            @RequestParam LocalDate fecha) {
        
        List<BloqueHorario> bloques = disponibilidadService
            .obtenerBloquesDisponibles(id, fecha);
        
        List<BloqueHorarioResponse> response = bloques.stream()
            .map(b -> BloqueHorarioResponse.builder()
                .id(b.getId())
                .fecha(b.getFecha())
                .horaInicio(b.getHoraInicio())
                .horaFin(b.getHoraFin())
                .disponible(b.getDisponible())
                .build())
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}