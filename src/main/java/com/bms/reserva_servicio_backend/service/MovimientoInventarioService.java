package com.bms.reserva_servicio_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bms.reserva_servicio_backend.models.ItemsInventario;
import com.bms.reserva_servicio_backend.models.MovimientoInventario;
import com.bms.reserva_servicio_backend.models.MovimientoInventario.TipoMovimiento;
import com.bms.reserva_servicio_backend.models.Reserva;
import com.bms.reserva_servicio_backend.models.User;
import com.bms.reserva_servicio_backend.repository.MovimientoInventarioRepository;

/**
 * Servicio para gestionar movimientos de inventario
 */
@Service
@Transactional
public class MovimientoInventarioService {

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    /**
     * Registrar un movimiento de inventario
     */
    public MovimientoInventario registrarMovimiento(
            ItemsInventario item,
            TipoMovimiento tipo,
            Integer cantidad,
            Reserva reserva,
            User usuario,
            String observaciones) {

        Integer stockAnterior = item.getCantidadDisponible();
        Integer stockPosterior = calcularStockPosterior(stockAnterior, tipo, cantidad);

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .item(item)
                .tipoMovimiento(tipo)
                .cantidad(cantidad)
                .reserva(reserva)
                .usuario(usuario)
                .observaciones(observaciones)
                .stockAnterior(stockAnterior)
                .stockPosterior(stockPosterior)
                .fechaMovimiento(LocalDateTime.now())
                .build();

        return movimientoRepository.save(movimiento);
    }

    /**
     * Calcular stock posterior según tipo de movimiento
     */
    private Integer calcularStockPosterior(Integer stockActual, TipoMovimiento tipo, Integer cantidad) {
        switch (tipo) {
            case ENTRADA:
            case DEVOLUCION:
            case AJUSTE_POSITIVO:
                return stockActual + cantidad;

            case SALIDA:
            case AJUSTE_NEGATIVO:
            case PERDIDA:
            case DANO:
                return stockActual - cantidad;

            default:
                return stockActual;
        }
    }

    /**
     * Obtener todos los movimientos
     */
    public List<MovimientoInventario> obtenerTodos() {
        return movimientoRepository.findAll();
    }

    /**
     * Obtener últimos 50 movimientos
     */
    public List<MovimientoInventario> obtenerUltimosMovimientos() {
        return movimientoRepository.findTop50ByOrderByFechaMovimientoDesc();
    }

    /**
     * Obtener movimientos por item
     */
    public List<MovimientoInventario> obtenerPorItem(Long itemId) {
        return movimientoRepository.findByItemIdOrderByFechaMovimientoDesc(itemId);
    }

    /**
     * Obtener movimientos por tipo
     */
    public List<MovimientoInventario> obtenerPorTipo(TipoMovimiento tipo) {
        return movimientoRepository.findByTipoMovimientoOrderByFechaMovimientoDesc(tipo);
    }

    /**
     * Obtener movimientos por reserva
     */
    public List<MovimientoInventario> obtenerPorReserva(Long reservaId) {
        return movimientoRepository.findByReservaIdOrderByFechaMovimientoDesc(reservaId);
    }

    /**
     * Obtener movimientos por usuario
     */
    public List<MovimientoInventario> obtenerPorUsuario(Long usuarioId) {
        return movimientoRepository.findByUsuarioIdOrderByFechaMovimientoDesc(usuarioId);
    }

    /**
     * Obtener movimientos en un rango de fechas
     */
    public List<MovimientoInventario> obtenerPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return movimientoRepository.findByFechaMovimientoBetween(inicio, fin);
    }

    /**
     * Obtener estadísticas de movimientos
     */
    public MovimientoEstadisticas obtenerEstadisticas() {
        Long totalEntradas = movimientoRepository.countByTipoMovimiento(TipoMovimiento.ENTRADA);
        Long totalSalidas = movimientoRepository.countByTipoMovimiento(TipoMovimiento.SALIDA);
        Long totalDevoluciones = movimientoRepository.countByTipoMovimiento(TipoMovimiento.DEVOLUCION);
        Long totalAjustes = movimientoRepository.countByTipoMovimiento(TipoMovimiento.AJUSTE_POSITIVO)
                + movimientoRepository.countByTipoMovimiento(TipoMovimiento.AJUSTE_NEGATIVO);

        return new MovimientoEstadisticas(totalEntradas, totalSalidas, totalDevoluciones, totalAjustes);
    }

    /**
     * Clase interna para estadísticas
     */
    public static class MovimientoEstadisticas {
        public final Long totalEntradas;
        public final Long totalSalidas;
        public final Long totalDevoluciones;
        public final Long totalAjustes;

        public MovimientoEstadisticas(Long entradas, Long salidas, Long devoluciones, Long ajustes) {
            this.totalEntradas = entradas;
            this.totalSalidas = salidas;
            this.totalDevoluciones = devoluciones;
            this.totalAjustes = ajustes;
        }
    }
}
