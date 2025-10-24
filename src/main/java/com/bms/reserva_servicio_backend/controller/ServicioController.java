package com.bms.reserva_servicio_backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.bms.reserva_servicio_backend.request.ServicioRequest;
import com.bms.reserva_servicio_backend.response.ServicioResponse;
import com.bms.reserva_servicio_backend.response.SuccessResponse;
import com.bms.reserva_servicio_backend.service.ServicioService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    /**
     * POST /api/servicios
     * Crear nuevo servicio de entretención
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<ServicioResponse>> crearServicio(
            @Valid @RequestBody ServicioRequest request) {
        SuccessResponse<ServicioResponse> response = servicioService.crearServicio(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/servicios
     * Obtener todos los servicios
     * Query params opcionales:
     * - estado: filtrar por estado (DISPONIBLE, MANTENIMIENTO, FUERA_SERVICIO)
     * - tipo: filtrar por tipo de servicio
     * - fecha: filtrar por disponibilidad en fecha específica
     */
    @GetMapping
    public ResponseEntity<List<ServicioResponse>> obtenerServicios(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        List<ServicioResponse> servicios;

        // Filtrar por disponibilidad en fecha
        if (fecha != null) {
            servicios = servicioService.obtenerDisponibles(fecha);
        }
        // Filtrar por tipo
        else if (tipo != null && !tipo.isEmpty()) {
            servicios = servicioService.obtenerPorTipo(tipo);
        }
        // Filtrar por estado
        else if (estado != null && !estado.isEmpty()) {
            servicios = servicioService.obtenerPorEstado(estado);
        }
        // Obtener todos
        else {
            servicios = servicioService.obtenerTodos();
        }

        return ResponseEntity.ok(servicios);
    }

    /**
     * GET /api/servicios/{id}
     * Obtener servicio por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponse> obtenerServicioPorId(@PathVariable Long id) {
        ServicioResponse servicio = servicioService.obtenerPorId(id);
        return ResponseEntity.ok(servicio);
    }

    /**
     * PUT /api/servicios/{id}
     * Actualizar servicio completo
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<ServicioResponse>> actualizarServicio(
            @PathVariable Long id,
            @Valid @RequestBody ServicioRequest request) {
        SuccessResponse<ServicioResponse> response = servicioService.actualizarServicio(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/servicios/{id}/estado
     * Cambiar solo el estado del servicio
     * Body: { "estado": "MANTENIMIENTO" }
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<ServicioResponse>> cambiarEstado(
            @PathVariable Long id,
            @RequestBody EstadoRequest estadoRequest) {
        SuccessResponse<ServicioResponse> response = servicioService.cambiarEstado(id, estadoRequest.getEstado());
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/servicios/{id}
     * Eliminar servicio (soft delete - pasa a FUERA_SERVICIO)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<String>> eliminarServicio(@PathVariable Long id) {
        SuccessResponse<String> response = servicioService.eliminarServicio(id);
        return ResponseEntity.ok(response);
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
