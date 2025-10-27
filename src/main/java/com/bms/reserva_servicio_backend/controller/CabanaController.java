package com.bms.reserva_servicio_backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bms.reserva_servicio_backend.models.ItemsInventario;
import com.bms.reserva_servicio_backend.request.CabanaRequest;
import com.bms.reserva_servicio_backend.response.CabanaResponse;
import com.bms.reserva_servicio_backend.response.ItemInventarioResponse;
import com.bms.reserva_servicio_backend.response.SuccessResponse;
import com.bms.reserva_servicio_backend.service.CabanaService;
import com.bms.reserva_servicio_backend.service.InventarioService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/cabanas")
public class CabanaController {

    @Autowired
    private CabanaService cabanaService;

    @Autowired
    private InventarioService inventarioService;

    /**
     * POST /api/cabanas
     * Crear nueva cabaña
     */
    @PostMapping
    public ResponseEntity<SuccessResponse<CabanaResponse>> crearCabana(
            @Valid @RequestBody CabanaRequest request) {
        SuccessResponse<CabanaResponse> response = cabanaService.crearCabana(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/cabanas
     * Obtener todas las cabañas
     * Query params opcionales:
     * - estado: filtrar por estado (DISPONIBLE, MANTENIMIENTO, FUERA_SERVICIO)
     * - fechaInicio & fechaFin: filtrar por disponibilidad en rango de fechas
     */
    @GetMapping
    public ResponseEntity<List<CabanaResponse>> obtenerCabanas(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        List<CabanaResponse> cabanas;

        // Filtrar por disponibilidad en fechas
        if (fechaInicio != null && fechaFin != null) {
            cabanas = cabanaService.obtenerDisponibles(fechaInicio, fechaFin);
        }
        // Filtrar por estado
        else if (estado != null && !estado.isEmpty()) {
            cabanas = cabanaService.obtenerPorEstado(estado);
        }
        // Obtener todas
        else {
            cabanas = cabanaService.obtenerTodas();
        }

        return ResponseEntity.ok(cabanas);
    }

    /**
     * GET /api/cabanas/{id}
     * Obtener cabaña por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CabanaResponse> obtenerCabanaPorId(@PathVariable Long id) {
        CabanaResponse cabana = cabanaService.obtenerPorId(id);
        return ResponseEntity.ok(cabana);
    }

    /**
     * PUT /api/cabanas/{id}
     * Actualizar cabaña completa
     */
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<CabanaResponse>> actualizarCabana(
            @PathVariable Long id,
            @Valid @RequestBody CabanaRequest request) {
        SuccessResponse<CabanaResponse> response = cabanaService.actualizarCabana(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/cabanas/{id}/estado
     * Cambiar solo el estado de la cabaña
     * Body: { "estado": "MANTENIMIENTO" }
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<SuccessResponse<CabanaResponse>> cambiarEstado(
            @PathVariable Long id,
            @RequestBody EstadoRequest estadoRequest) {
        SuccessResponse<CabanaResponse> response = cabanaService.cambiarEstado(id, estadoRequest.getEstado());
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/cabanas/{id}
     * Eliminar cabaña (soft delete - pasa a FUERA_SERVICIO)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<String>> eliminarCabana(@PathVariable Long id) {
        SuccessResponse<String> response = cabanaService.eliminarCabana(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/cabanas/{id}/items-adicionales
     * Obtener items adicionales reservables de una cabaña
     */
    @GetMapping("/{id}/items-adicionales")
    public ResponseEntity<List<ItemInventarioResponse>> obtenerItemsAdicionales(@PathVariable Long id) {
        // Obtener todos los items del recurso (cabaña)
        List<ItemsInventario> items = inventarioService.obtenerItemsPorRecurso(id);

        // Filtrar solo los que son reservables y mapear a DTO
        List<ItemInventarioResponse> itemsReservables = items.stream()
            .filter(item -> item.getEsReservable() != null && item.getEsReservable())
            .filter(item -> item.getCantidadDisponible() != null && item.getCantidadDisponible() > 0)
            .map(item -> ItemInventarioResponse.builder()
                .id(item.getId())
                .nombre(item.getNombre())
                .categoria(item.getCategoria())
                .cantidadTotal(item.getCantidadTotal())
                .cantidadDisponible(item.getCantidadDisponible())
                .estadoItem(item.getEstadoItem() != null ? item.getEstadoItem().name() : null)
                .esReservable(item.getEsReservable())
                .precioReserva(item.getPrecioReserva())
                .recursoId(item.getRecurso() != null ? item.getRecurso().getId() : null)
                .nombreRecurso(item.getRecurso() != null ? item.getRecurso().getNombre() : null)
                .build())
            .toList();

        return ResponseEntity.ok(itemsReservables);
    }

    // Clase interna para recibir el estado en PATCH
    static class EstadoRequest {
        private String estado;

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }
    }

}
