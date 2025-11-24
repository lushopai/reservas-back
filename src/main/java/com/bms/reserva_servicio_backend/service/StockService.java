package com.bms.reserva_servicio_backend.service;

import com.bms.reserva_servicio_backend.models.ItemsInventario;
import com.bms.reserva_servicio_backend.models.MovimientoInventario;
import com.bms.reserva_servicio_backend.models.MovimientoInventario.TipoMovimiento;
import com.bms.reserva_servicio_backend.repository.ItemInventarioRepository;
import com.bms.reserva_servicio_backend.repository.MovimientoInventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class StockService {

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Transactional
    public boolean reservarStock(Map<Long, Integer> itemsToReserve) {
        for (Map.Entry<Long, Integer> entry : itemsToReserve.entrySet()) {
            Long itemId = entry.getKey();
            Integer quantity = entry.getValue();

            Optional<ItemsInventario> optionalItem = itemInventarioRepository.findById(itemId);
            if (optionalItem.isEmpty()) {
                throw new IllegalArgumentException("Item de inventario con ID " + itemId + " no encontrado.");
            }

            ItemsInventario item = optionalItem.get();
            if (item.getCantidadDisponible() < quantity) {
                throw new IllegalStateException("Stock insuficiente para el item: " + item.getNombre()
                        + ". Disponible: " + item.getCantidadDisponible() + ", Solicitado: " + quantity);
            }

            int stockAnterior = item.getCantidadDisponible();
            item.setCantidadDisponible(item.getCantidadDisponible() - quantity);
            itemInventarioRepository.save(item);

            MovimientoInventario movimiento = MovimientoInventario.builder()
                    .item(item)
                    .tipoMovimiento(TipoMovimiento.SALIDA)
                    .cantidad(quantity)
                    .fechaMovimiento(LocalDateTime.now())
                    .observaciones("Reserva de stock para paquete/reserva")
                    .stockAnterior(stockAnterior)
                    .stockPosterior(item.getCantidadDisponible())
                    .build();
            movimientoInventarioRepository.save(movimiento);
        }
        return true;
    }

    @Transactional
    public boolean liberarStock(Map<Long, Integer> itemsToRelease,
            com.bms.reserva_servicio_backend.models.Reserva reserva) {
        for (Map.Entry<Long, Integer> entry : itemsToRelease.entrySet()) {
            Long itemId = entry.getKey();
            Integer quantity = entry.getValue();

            Optional<ItemsInventario> optionalItem = itemInventarioRepository.findById(itemId);
            if (optionalItem.isEmpty()) {
                throw new IllegalArgumentException("Item de inventario con ID " + itemId + " no encontrado.");
            }

            ItemsInventario item = optionalItem.get();

            int stockAnterior = item.getCantidadDisponible();
            item.setCantidadDisponible(item.getCantidadDisponible() + quantity);
            itemInventarioRepository.save(item);

            MovimientoInventario movimiento = MovimientoInventario.builder()
                    .item(item)
                    .tipoMovimiento(TipoMovimiento.DEVOLUCION)
                    .cantidad(quantity)
                    .fechaMovimiento(LocalDateTime.now())
                    .observaciones("Liberación de stock por cancelación/finalización de reserva")
                    .stockAnterior(stockAnterior)
                    .stockPosterior(item.getCantidadDisponible())
                    .reserva(reserva)
                    .usuario(reserva != null ? reserva.getUser() : null)
                    .build();
            movimientoInventarioRepository.save(movimiento);
        }
        return true;
    }
}
