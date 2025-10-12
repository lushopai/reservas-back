package com.bms.reserva_servicio_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bms.reserva_servicio_backend.mappers.ReservaMapper;
import com.bms.reserva_servicio_backend.models.PaqueteReserva;
import com.bms.reserva_servicio_backend.request.PagoRequest;
import com.bms.reserva_servicio_backend.request.PaqueteReservaRequest;
import com.bms.reserva_servicio_backend.response.PaqueteResponse;
import com.bms.reserva_servicio_backend.response.SuccessResponse;
import com.bms.reserva_servicio_backend.service.PaqueteReservaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/paquetes")
@CrossOrigin(origins = "*")
public class PaqueteController {
    
    @Autowired
    private PaqueteReservaService paqueteService;
    
    @Autowired
    private ReservaMapper mapper;
    
    /**
     * POST /api/paquetes
     * Crear paquete combinado (cabaña + servicios)
     * @throws Exception 
     */
    @PostMapping
    public ResponseEntity<SuccessResponse<PaqueteResponse>> crearPaquete(
            @Valid @RequestBody PaqueteReservaRequest request) throws Exception {
        
        // El servicio internamente llama:
        // 1. ReservaService.reservarCabana() si incluye cabaña
        // 2. ReservaService.reservarServicio() por cada servicio
        // 3. PrecioService.calcularDescuentoPaquete()
        // Cada uno de esos a su vez usa:
        // - ValidacionService
        // - DisponibilidadService
        // - InventarioService
        // - PrecioService
        
        PaqueteReserva paquete = paqueteService.crearPaqueteCompleto(
            request.getClienteId(),
            request.toDTO()
        );
        
        PaqueteResponse response = mapper.toPaqueteResponse(paquete);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(SuccessResponse.of(response, "Paquete creado exitosamente"));
    }
    
    /**
     * PUT /api/paquetes/{id}/confirmar
     * Confirmar paquete completo con pago
     */
    @PutMapping("/{id}/confirmar")
    public ResponseEntity<SuccessResponse<PaqueteResponse>> confirmarPaquete(
            @PathVariable Long id,
            @Valid @RequestBody PagoRequest pagoRequest) {
        
        // El servicio maneja:
        // - PagoService.procesarPagoPaquete()
        // - Confirma todas las reservas del paquete
        PaqueteReserva paquete = paqueteService.confirmarPaquete(id, pagoRequest.toDTO());
        
        PaqueteResponse response = mapper.toPaqueteResponse(paquete);
        
        return ResponseEntity.ok(
            SuccessResponse.of(response, "Paquete confirmado exitosamente")
        );
    }
    
    /**
     * GET /api/paquetes/{id}
     * Obtener detalles de un paquete
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaqueteResponse> obtenerPaquete(@PathVariable Long id) {
        PaqueteReserva paquete = paqueteService.obtenerPorId(id);
        return ResponseEntity.ok(mapper.toPaqueteResponse(paquete));
    }
    
    
}
