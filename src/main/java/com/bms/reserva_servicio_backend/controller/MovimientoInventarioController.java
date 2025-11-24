package com.bms.reserva_servicio_backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bms.reserva_servicio_backend.models.MovimientoInventario;
import com.bms.reserva_servicio_backend.models.MovimientoInventario.TipoMovimiento;
import com.bms.reserva_servicio_backend.response.MovimientoInventarioResponse;
import com.bms.reserva_servicio_backend.service.MovimientoInventarioService;

@RestController
@RequestMapping("/api/movimientos-inventario")
@CrossOrigin(origins = "http://localhost:4200")
public class MovimientoInventarioController {

        @Autowired
        private MovimientoInventarioService movimientoService;

        /**
         * GET /api/movimientos-inventario
         * Obtener todos los movimientos (últimos 50)
         */
        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
        public ResponseEntity<List<MovimientoInventarioResponse>> obtenerMovimientos() {
                List<MovimientoInventario> movimientos = movimientoService.obtenerUltimosMovimientos();
                List<MovimientoInventarioResponse> response = movimientos.stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(response);
        }

        /**
         * GET /api/movimientos-inventario/item/{itemId}
         * Obtener movimientos por item
         */
        @GetMapping("/item/{itemId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
        public ResponseEntity<List<MovimientoInventarioResponse>> obtenerPorItem(@PathVariable Long itemId) {
                List<MovimientoInventario> movimientos = movimientoService.obtenerPorItem(itemId);
                List<MovimientoInventarioResponse> response = movimientos.stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(response);
        }

        /**
         * GET /api/movimientos-inventario/tipo/{tipo}
         * Obtener movimientos por tipo
         */
        @GetMapping("/tipo/{tipo}")
        @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
        public ResponseEntity<List<MovimientoInventarioResponse>> obtenerPorTipo(@PathVariable String tipo) {
                TipoMovimiento tipoMovimiento = TipoMovimiento.valueOf(tipo.toUpperCase());
                List<MovimientoInventario> movimientos = movimientoService.obtenerPorTipo(tipoMovimiento);
                List<MovimientoInventarioResponse> response = movimientos.stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(response);
        }

        /**
         * GET /api/movimientos-inventario/reserva/{reservaId}
         * Obtener movimientos por reserva
         */
        @GetMapping("/reserva/{reservaId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
        public ResponseEntity<List<MovimientoInventarioResponse>> obtenerPorReserva(@PathVariable Long reservaId) {
                List<MovimientoInventario> movimientos = movimientoService.obtenerPorReserva(reservaId);
                List<MovimientoInventarioResponse> response = movimientos.stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(response);
        }

        /**
         * GET /api/movimientos-inventario/rango
         * Obtener movimientos en un rango de fechas
         */
        @GetMapping("/rango")
        @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
        public ResponseEntity<List<MovimientoInventarioResponse>> obtenerPorRango(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

                List<MovimientoInventario> movimientos = movimientoService.obtenerPorRangoFechas(inicio, fin);
                List<MovimientoInventarioResponse> response = movimientos.stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(response);
        }

        /**
         * GET /api/movimientos-inventario/estadisticas
         * Obtener estadísticas de movimientos
         */
        @GetMapping("/estadisticas")
        @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
        public ResponseEntity<MovimientoInventarioService.MovimientoEstadisticas> obtenerEstadisticas() {
                return ResponseEntity.ok(movimientoService.obtenerEstadisticas());
        }

        /**
         * Mapper de Movimiento a Response
         */
        private MovimientoInventarioResponse mapToResponse(MovimientoInventario movimiento) {
                try {
                        return MovimientoInventarioResponse.builder()
                                        .id(movimiento.getId())
                                        .itemId(movimiento.getItem() != null ? movimiento.getItem().getId() : null)
                                        .nombreItem(movimiento.getItem() != null ? movimiento.getItem().getNombre()
                                                        : null)
                                        .categoriaItem(movimiento.getItem() != null
                                                        ? movimiento.getItem().getCategoria()
                                                        : null)
                                        .tipoMovimiento(movimiento.getTipoMovimiento() != null
                                                        ? movimiento.getTipoMovimiento().name()
                                                        : "DESCONOCIDO")
                                        .cantidad(movimiento.getCantidad())
                                        .fechaMovimiento(movimiento.getFechaMovimiento())
                                        .stockAnterior(movimiento.getStockAnterior())
                                        .stockPosterior(movimiento.getStockPosterior())
                                        .reservaId(movimiento.getReserva() != null ? movimiento.getReserva().getId()
                                                        : null)
                                        .nombreRecurso(movimiento.getReserva() != null
                                                        && movimiento.getReserva().getRecurso() != null
                                                                        ? movimiento.getReserva().getRecurso()
                                                                                        .getNombre()
                                                                        : null)
                                        .usuarioId(movimiento.getUsuario() != null ? movimiento.getUsuario().getId()
                                                        : null)
                                        .nombreUsuario(movimiento.getUsuario() != null
                                                        ? movimiento.getUsuario().getNombres() + " "
                                                                        + movimiento.getUsuario().getApellidos()
                                                        : null)
                                        .observaciones(movimiento.getObservaciones())
                                        .build();
                } catch (Exception e) {
                        System.err.println("Error mapping movimiento ID: " + movimiento.getId());
                        e.printStackTrace();
                        throw e;
                }
        }
}
