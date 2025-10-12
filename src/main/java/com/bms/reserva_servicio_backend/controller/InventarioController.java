package com.bms.reserva_servicio_backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bms.reserva_servicio_backend.models.ItemsInventario;
import com.bms.reserva_servicio_backend.request.ItemInventarioRequest;
import com.bms.reserva_servicio_backend.response.DisponibilidadItemResponse;
import com.bms.reserva_servicio_backend.response.ItemInventarioResponse;
import com.bms.reserva_servicio_backend.response.SuccessResponse;
import com.bms.reserva_servicio_backend.service.InventarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventario")
@CrossOrigin(origins = "*")
public class InventarioController {
    
    @Autowired
    private InventarioService inventarioService;
    
    /**
     * GET /api/inventario/recurso/{recursoId}
     * Obtener inventario de un recurso
     */
    @GetMapping("/recurso/{recursoId}")
    public ResponseEntity<List<ItemInventarioResponse>> obtenerInventarioRecurso(
            @PathVariable Long recursoId) {
        
        List<ItemsInventario> items = inventarioService.obtenerItemsPorRecurso(recursoId);
        
        List<ItemInventarioResponse> response = items.stream()
            .map(item -> ItemInventarioResponse.builder()
                .id(item.getId())
                .nombre(item.getNombre())
                .categoria(item.getCategoria())
                .cantidadTotal(item.getCantidadTotal())
                .cantidadDisponible(item.getCantidadDisponible())
                .estadoItem(item.getEstadoItem())
                .esReservable(item.getEsReservable())
                .precioReserva(item.getPrecioReserva())
                .build())
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/inventario/{id}/disponibilidad
     * Consultar disponibilidad de un item en un período
     */
    @GetMapping("/{id}/disponibilidad")
    public ResponseEntity<DisponibilidadItemResponse> consultarDisponibilidadItem(
            @PathVariable Long id,
            @RequestParam LocalDateTime fechaInicio,
            @RequestParam LocalDateTime fechaFin) {
        
        // El servicio calcula cuántos están reservados
        // y resta del total
        int cantidadDisponible = inventarioService
            .calcularDisponibilidadItem(id, fechaInicio, fechaFin);
        
        ItemsInventario item = inventarioService.obtenerPorId(id);
        
        DisponibilidadItemResponse response = DisponibilidadItemResponse.builder()
            .itemId(id)
            .nombreItem(item.getNombre())
            .cantidadTotal(item.getCantidadTotal())
            .cantidadDisponible(cantidadDisponible)
            .disponible(cantidadDisponible > 0)
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/inventario
     * Agregar item al inventario
     */
    @PostMapping
    public ResponseEntity<SuccessResponse<ItemInventarioResponse>> agregarItem(
            @Valid @RequestBody ItemInventarioRequest request) {
        
        ItemsInventario item = inventarioService.agregarItem(request.toEntity());
        
        ItemInventarioResponse response = ItemInventarioResponse.builder()
            .id(item.getId())
            .nombre(item.getNombre())
            .categoria(item.getCategoria())
            .cantidadTotal(item.getCantidadTotal())
            .build();
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(SuccessResponse.of(response, "Item agregado exitosamente"));
    }
}
