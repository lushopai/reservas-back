package com.bms.reserva_servicio_backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bms.reserva_servicio_backend.models.Pagos;
import com.bms.reserva_servicio_backend.models.PaqueteReserva;
import com.bms.reserva_servicio_backend.models.Reserva;
import com.bms.reserva_servicio_backend.repository.PaqueteReservaRepository;
import com.bms.reserva_servicio_backend.repository.ReservaRepository;
import com.bms.reserva_servicio_backend.request.PagoRequest;
import com.bms.reserva_servicio_backend.response.PagoResponse;
import com.bms.reserva_servicio_backend.response.SuccessResponse;
import com.bms.reserva_servicio_backend.service.PagoService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PaqueteReservaRepository paqueteReservaRepository;

    /**
     * GET /api/pagos
     * Obtener todos los pagos (solo ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PagoResponse>> obtenerTodosPagos() {
        List<Pagos> pagos = pagoService.obtenerTodos();

        List<PagoResponse> responses = pagos.stream()
                .map(pago -> {
                    if (pago.getReserva() != null) {
                        return mapToPagoResponse(pago);
                    } else {
                        return mapToPagoResponsePaquete(pago);
                    }
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * POST /api/pagos/reserva/{id}
     * Procesar pago de una reserva individual
     */
    @PostMapping("/reserva/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<SuccessResponse<PagoResponse>> procesarPagoReserva(
            @PathVariable Long id,
            @Valid @RequestBody PagoRequest request) {

        // Buscar reserva
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada con ID: " + id));

        // Validar que el monto coincida con el total de la reserva
        if (request.getMonto().compareTo(reserva.getPrecioTotal()) != 0) {
            return ResponseEntity.badRequest()
                    .body(SuccessResponse.of(null, "El monto no coincide con el total de la reserva"));
        }

        // Validar que la reserva esté en estado PENDIENTE o CONFIRMADA
        if (!"PENDIENTE".equals(reserva.getEstado()) && !"CONFIRMADA".equals(reserva.getEstado())) {
            return ResponseEntity.badRequest()
                    .body(SuccessResponse.of(null, "La reserva no puede ser pagada en su estado actual: " + reserva.getEstado()));
        }

        // Procesar pago
        Pagos pago = pagoService.procesarPago(reserva, request.toDTO());

        // Actualizar estado de reserva a CONFIRMADA si estaba PENDIENTE
        if ("PENDIENTE".equals(reserva.getEstado())) {
            reserva.setEstado("CONFIRMADA");
            reservaRepository.save(reserva);
        }

        // Construir response
        PagoResponse response = mapToPagoResponse(pago);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(response, "Pago procesado exitosamente"));
    }

    /**
     * POST /api/pagos/paquete/{id}
     * Procesar pago de un paquete
     */
    @PostMapping("/paquete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<SuccessResponse<PagoResponse>> procesarPagoPaquete(
            @PathVariable Long id,
            @Valid @RequestBody PagoRequest request) {

        // Buscar paquete
        PaqueteReserva paquete = paqueteReservaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado con ID: " + id));

        // Validar que el monto coincida con el precio final del paquete
        if (request.getMonto().compareTo(paquete.getPrecioFinal()) != 0) {
            return ResponseEntity.badRequest()
                    .body(SuccessResponse.of(null, "El monto no coincide con el total del paquete"));
        }

        // Validar que el paquete esté en estado PENDIENTE o CONFIRMADO
        if (!"PENDIENTE".equals(paquete.getEstado()) && !"CONFIRMADO".equals(paquete.getEstado())) {
            return ResponseEntity.badRequest()
                    .body(SuccessResponse.of(null, "El paquete no puede ser pagado en su estado actual: " + paquete.getEstado()));
        }

        // Procesar pago
        Pagos pago = pagoService.procesarPagoPaquete(paquete, request.toDTO());

        // Actualizar estado del paquete a CONFIRMADO si estaba PENDIENTE
        if ("PENDIENTE".equals(paquete.getEstado())) {
            paquete.setEstado("CONFIRMADO");
            paqueteReservaRepository.save(paquete);
        }

        // Construir response
        PagoResponse response = mapToPagoResponsePaquete(pago);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.of(response, "Pago del paquete procesado exitosamente"));
    }

    /**
     * GET /api/pagos/reserva/{id}
     * Obtener pagos de una reserva
     */
    @GetMapping("/reserva/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<SuccessResponse<List<PagoResponse>>> obtenerPagosReserva(@PathVariable Long id) {

        List<Pagos> pagos = pagoService.obtenerPagosPorReserva(id);

        List<PagoResponse> responses = pagos.stream()
                .map(this::mapToPagoResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(SuccessResponse.of(responses, "Pagos obtenidos exitosamente"));
    }

    /**
     * GET /api/pagos/paquete/{id}
     * Obtener pagos de un paquete
     */
    @GetMapping("/paquete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<SuccessResponse<List<PagoResponse>>> obtenerPagosPaquete(@PathVariable Long id) {

        List<Pagos> pagos = pagoService.obtenerPagosPorPaquete(id);

        List<PagoResponse> responses = pagos.stream()
                .map(this::mapToPagoResponsePaquete)
                .collect(Collectors.toList());

        return ResponseEntity.ok(SuccessResponse.of(responses, "Pagos del paquete obtenidos exitosamente"));
    }

    /**
     * GET /api/pagos/{id}
     * Obtener un pago por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<SuccessResponse<PagoResponse>> obtenerPago(@PathVariable Long id) {

        Pagos pago = pagoService.obtenerPagosPorReserva(id).stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con ID: " + id));

        PagoResponse response = pago.getReserva() != null
                ? mapToPagoResponse(pago)
                : mapToPagoResponsePaquete(pago);

        return ResponseEntity.ok(SuccessResponse.of(response, "Pago obtenido exitosamente"));
    }

    /**
     * Mapear Pagos a PagoResponse (para reservas)
     */
    private PagoResponse mapToPagoResponse(Pagos pago) {
        return PagoResponse.builder()
                .id(pago.getId())
                .reservaId(pago.getReserva() != null ? pago.getReserva().getId() : null)
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago())
                .estado(pago.getEstado())
                .transaccionId(pago.getTransaccionId())
                .fechaPago(pago.getFechaPago())
                .nombreRecurso(pago.getReserva() != null && pago.getReserva().getRecurso() != null
                        ? pago.getReserva().getRecurso().getNombre()
                        : null)
                .tipoReserva(pago.getReserva() != null ? pago.getReserva().getTipoReserva() : null)
                .build();
    }

    /**
     * Mapear Pagos a PagoResponse (para paquetes)
     */
    private PagoResponse mapToPagoResponsePaquete(Pagos pago) {
        return PagoResponse.builder()
                .id(pago.getId())
                .paqueteId(pago.getPaquete() != null ? pago.getPaquete().getId() : null)
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago())
                .estado(pago.getEstado())
                .transaccionId(pago.getTransaccionId())
                .fechaPago(pago.getFechaPago())
                .nombreRecurso(pago.getPaquete() != null ? pago.getPaquete().getNombrePaquete() : null)
                .tipoReserva("PAQUETE")
                .build();
    }
}
