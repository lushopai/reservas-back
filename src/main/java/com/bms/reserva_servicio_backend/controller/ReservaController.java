package com.bms.reserva_servicio_backend.controller;

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

import com.bms.reserva_servicio_backend.mappers.ReservaMapper;
import com.bms.reserva_servicio_backend.models.Reserva;
import com.bms.reserva_servicio_backend.request.CambioEstadoRequest;
import com.bms.reserva_servicio_backend.request.PagoRequest;
import com.bms.reserva_servicio_backend.request.ReservaCabanaRequest;
import com.bms.reserva_servicio_backend.request.ReservaServicioRequest;
import com.bms.reserva_servicio_backend.response.ReservaResponse;
import com.bms.reserva_servicio_backend.response.SuccessResponse;
import com.bms.reserva_servicio_backend.service.ReservaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "http://localhost:4200")
public class ReservaController {

    // ===== INYECCIÓN DE SERVICIOS =====
    private final ReservaService reservaService;
    private final ReservaMapper mapper;

    @Autowired
    public ReservaController(ReservaService reservaService, ReservaMapper mapper) {
        this.reservaService = reservaService;
        this.mapper = mapper;
    }

    // ===== ENDPOINTS =====

    /**
     * POST /api/reservas/cabana
     * Crear reserva de cabaña
     * @throws Exception 
     */
    @PostMapping("/cabana")
    public ResponseEntity<SuccessResponse<ReservaResponse>> reservarCabana(
            @Valid @RequestBody ReservaCabanaRequest request) {

        // El servicio maneja:
        // - ValidacionService.validarReservaCabana()
        // - DisponibilidadService.validarDisponibilidadCabana()
        // - PrecioService.calcularPrecioCabana()
        // - InventarioService (si hay items)
        Reserva reserva = reservaService.reservarCabaña(
                request.getCabanaId(),
                request.getClienteId(),
                request.getFechaInicio(),
                request.getFechaFin(),
                request.getItemsAdicionales());

        ReservaResponse response = mapper.toResponse(reserva);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.of(response, "Reserva de cabaña creada exitosamente"));
    }

    /**
     * POST /api/reservas/servicio
     * Crear reserva de servicio
     * @throws Exception 
     */
    @PostMapping("/servicio")
    public ResponseEntity<SuccessResponse<ReservaResponse>> reservarServicio(
            @Valid @RequestBody ReservaServicioRequest request) {

        // El servicio maneja:
        // - ValidacionService.validarReservaServicio()
        // - DisponibilidadService.validarDisponibilidadServicio()
        // - PrecioService.calcularPrecioServicio()
        Reserva reserva = reservaService.reservarServicio(
                request.getServicioId(),
                request.getClienteId(),
                request.getFecha(),
                request.getHoraInicio(),
                request.getDuracionBloques(),
                request.getEquipamiento());

        ReservaResponse response = mapper.toResponse(reserva);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.of(response, "Reserva de servicio creada exitosamente"));
    }

    /**
     * PUT /api/reservas/{id}/confirmar
     * Confirmar reserva con pago
     */
    @PutMapping("/{id}/confirmar")
    public ResponseEntity<SuccessResponse<ReservaResponse>> confirmarReserva(
            @PathVariable Long id,
            @Valid @RequestBody PagoRequest pagoRequest) {

        // El servicio maneja:
        // - ValidacionService.validarPago()
        // - PagoService.procesarPago()
        Reserva reserva = reservaService.confirmarReserva(id, pagoRequest.toDTO());

        ReservaResponse response = mapper.toResponse(reserva);

        return ResponseEntity.ok(
                SuccessResponse.of(response, "Reserva confirmada exitosamente"));
    }

    /**
     * DELETE /api/reservas/{id}
     * Cancelar reserva
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> cancelarReserva(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {

        // El servicio maneja:
        // - InventarioService.liberarItems()
        // - DisponibilidadService.liberarRecurso()
        reservaService.cancelarReserva(id, motivo);

        return ResponseEntity.ok(
                SuccessResponse.of(null, "Reserva cancelada exitosamente"));
    }

    /**
     * GET /api/reservas/{id}
     * Obtener detalles de una reserva
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> obtenerReserva(@PathVariable Long id) {
        Reserva reserva = reservaService.obtenerPorId(id);
        return ResponseEntity.ok(mapper.toResponse(reserva));
    }

    /**
     * GET /api/reservas/cliente/{clienteId}
     * Obtener todas las reservas de un cliente
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ReservaResponse>> obtenerReservasCliente(
            @PathVariable Long clienteId) {

        List<Reserva> reservas = reservaService.obtenerReservasPorCliente(clienteId);

        List<ReservaResponse> response = reservas.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/reservas
     * Obtener todas las reservas (para admin)
     */
    @GetMapping
    public ResponseEntity<List<ReservaResponse>> obtenerTodasLasReservas(
            @RequestParam(required = false) String estado) {

        List<Reserva> reservas;
        if (estado != null && !estado.isEmpty()) {
            reservas = reservaService.obtenerPorEstado(estado);
        } else {
            reservas = reservaService.obtenerTodas();
        }

        List<ReservaResponse> response = reservas.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/reservas/{id}/estado
     * Cambiar estado de una reserva (Admin)
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<SuccessResponse<ReservaResponse>> cambiarEstadoReserva(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoRequest request) {

        Reserva reserva = reservaService.cambiarEstado(
                id,
                request.getNuevoEstado(),
                request.getMotivo(),
                request.getObservaciones()
        );

        ReservaResponse response = mapper.toResponse(reserva);

        return ResponseEntity.ok(
                SuccessResponse.of(response, "Estado de reserva actualizado exitosamente"));
    }

}
