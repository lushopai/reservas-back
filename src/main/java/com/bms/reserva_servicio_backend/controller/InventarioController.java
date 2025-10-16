package com.bms.reserva_servicio_backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

        // Convertir request a entidad y asignar recurso
        ItemsInventario item = inventarioService.agregarItemConRecurso(request);

        ItemInventarioResponse response = ItemInventarioResponse.builder()
            .id(item.getId())
            .nombre(item.getNombre())
            .categoria(item.getCategoria())
            .cantidadTotal(item.getCantidadTotal())
            .cantidadDisponible(item.getCantidadDisponible())
            .estadoItem(item.getEstadoItem())
            .esReservable(item.getEsReservable())
            .precioReserva(item.getPrecioReserva())
            .recursoId(item.getRecurso() != null ? item.getRecurso().getId() : null)
            .nombreRecurso(item.getRecurso() != null ? item.getRecurso().getNombre() : null)
            .build();

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(SuccessResponse.of(response, "Item agregado exitosamente"));
    }

    /**
     * GET /api/inventario
     * Obtener todos los items del inventario
     */
    @GetMapping
    public ResponseEntity<List<ItemInventarioResponse>> obtenerTodos() {
        List<ItemsInventario> items = inventarioService.obtenerTodos();

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
                .recursoId(item.getRecurso() != null ? item.getRecurso().getId() : null)
                .nombreRecurso(item.getRecurso() != null ? item.getRecurso().getNombre() : null)
                .build())
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/inventario/{id}
     * Obtener item por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemInventarioResponse> obtenerPorId(@PathVariable Long id) {
        ItemsInventario item = inventarioService.obtenerPorId(id);

        ItemInventarioResponse response = ItemInventarioResponse.builder()
            .id(item.getId())
            .nombre(item.getNombre())
            .categoria(item.getCategoria())
            .cantidadTotal(item.getCantidadTotal())
            .cantidadDisponible(item.getCantidadDisponible())
            .estadoItem(item.getEstadoItem())
            .esReservable(item.getEsReservable())
            .precioReserva(item.getPrecioReserva())
            .recursoId(item.getRecurso() != null ? item.getRecurso().getId() : null)
            .nombreRecurso(item.getRecurso() != null ? item.getRecurso().getNombre() : null)
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/inventario/{id}
     * Actualizar item del inventario
     */
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<ItemInventarioResponse>> actualizarItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemInventarioRequest request) {

        ItemsInventario itemActualizado = inventarioService.actualizarItemConRecurso(id, request);

        ItemInventarioResponse response = ItemInventarioResponse.builder()
            .id(itemActualizado.getId())
            .nombre(itemActualizado.getNombre())
            .categoria(itemActualizado.getCategoria())
            .cantidadTotal(itemActualizado.getCantidadTotal())
            .cantidadDisponible(itemActualizado.getCantidadDisponible())
            .estadoItem(itemActualizado.getEstadoItem())
            .esReservable(itemActualizado.getEsReservable())
            .precioReserva(itemActualizado.getPrecioReserva())
            .recursoId(itemActualizado.getRecurso() != null ? itemActualizado.getRecurso().getId() : null)
            .nombreRecurso(itemActualizado.getRecurso() != null ? itemActualizado.getRecurso().getNombre() : null)
            .build();

        return ResponseEntity.ok(SuccessResponse.of(response, "Item actualizado exitosamente"));
    }

    /**
     * DELETE /api/inventario/{id}
     * Eliminar item del inventario
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<String>> eliminarItem(@PathVariable Long id) {
        inventarioService.eliminarItem(id);
        return ResponseEntity.ok(SuccessResponse.of("Item eliminado", "Item eliminado exitosamente del inventario"));
    }
}
