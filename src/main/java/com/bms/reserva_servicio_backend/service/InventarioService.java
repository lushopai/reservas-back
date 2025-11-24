package com.bms.reserva_servicio_backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bms.reserva_servicio_backend.dto.ItemReservaDTO;
import com.bms.reserva_servicio_backend.enums.EstadoItem;
import com.bms.reserva_servicio_backend.enums.EstadoReserva;
import com.bms.reserva_servicio_backend.models.ItemReservado;
import com.bms.reserva_servicio_backend.models.ItemsInventario;
import com.bms.reserva_servicio_backend.models.Reserva;
import com.bms.reserva_servicio_backend.repository.ItemInventarioRepository;
import com.bms.reserva_servicio_backend.repository.ItemReservadoRepository;
import com.bms.reserva_servicio_backend.repository.RecursoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class InventarioService {

    @Autowired
    private ItemInventarioRepository itemRepository;

    @Autowired
    private ItemReservadoRepository itemReservadoRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private StockService stockService; // Inject StockService

    /**
     * Validar disponibilidad de items
     */
    public boolean validarDisponibilidadItems(List<ItemReservaDTO> items,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {
        if (items == null || items.isEmpty()) {
            return true;
        }

        for (ItemReservaDTO itemDTO : items) {
            ItemsInventario item = itemRepository.findById(itemDTO.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Item no encontrado: " + itemDTO.getItemId()));

            // Calcular cuántos están reservados en ese período
            Integer cantidadReservada = itemReservadoRepository
                    .countReservadasEnPeriodo(itemDTO.getItemId(), fechaInicio, fechaFin);

            int disponible = item.getCantidadTotal() - cantidadReservada;

            if (disponible < itemDTO.getCantidad()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reservar items
     */
    public void reservarItems(Reserva reserva, List<ItemReservaDTO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        Map<Long, Integer> itemsToReserve = new HashMap<>();

        for (ItemReservaDTO itemDTO : items) {
            ItemsInventario item = itemRepository.findById(itemDTO.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Item no encontrado: " + itemDTO.getItemId()));

            // VALIDACIÓN 1: Cantidad solicitada debe ser positiva
            if (itemDTO.getCantidad() <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad a reservar para el item " + item.getNombre() + " debe ser positiva.");
            }

            // VALIDACIÓN 2: Verificar que el item pertenezca al mismo recurso de la reserva
            if (!item.getRecurso().getId().equals(reserva.getRecurso().getId())) {
                throw new IllegalArgumentException(
                        String.format("El item '%s' (ID: %d) pertenece al recurso '%s' (ID: %d), " +
                                "pero la reserva es para el recurso '%s' (ID: %d). " +
                                "No se pueden reservar items de otros recursos.",
                                item.getNombre(), item.getId(),
                                item.getRecurso().getNombre(), item.getRecurso().getId(),
                                reserva.getRecurso().getNombre(), reserva.getRecurso().getId()));
            }

            // VALIDACIÓN 3: El item debe ser reservable si se está reservando de forma
            // individual (no como parte de un paquete predefinido)
            // Asumo que si llega aquí, se está intentando reservar un item 'adicional'
            if (!item.getEsReservable()) {
                throw new IllegalStateException("El item '" + item.getNombre() + "' no es reservable individualmente.");
            }

            // VALIDACIÓN 4: El estado del item debe ser 'disponible' para poder ser
            // reservado
            if (!item.getEstadoItem().estaDisponible()) {
                throw new IllegalStateException("El item '" + item.getNombre()
                        + "' no está en un estado que permita su reserva (Estado actual: " + item.getEstadoItem()
                        + ").");
            }

            // La validación de disponibilidad de stock ya se hace en
            // StockService.reservarStock
            itemsToReserve.put(item.getId(), itemDTO.getCantidad());

            ItemReservado itemReservado = new ItemReservado();
            itemReservado.setReserva(reserva);
            itemReservado.setItem(item);
            itemReservado.setCantidad(itemDTO.getCantidad());
            itemReservado
                    .setPrecioUnitario(item.getPrecioReserva() != null ? item.getPrecioReserva() : BigDecimal.ZERO);
            itemReservado.setSubtotal(itemReservado.getPrecioUnitario()
                    .multiply(new BigDecimal(itemDTO.getCantidad())));

            itemReservadoRepository.save(itemReservado);
        }
        // Use StockService to reserve stock
        stockService.reservarStock(itemsToReserve);
    }

    /**
     * Liberar items - Reponer stock al cancelar o finalizar reserva
     */
    public void liberarItems(Reserva reserva) {
        // Obtener items reservados de esta reserva
        List<ItemReservado> itemsReservados = itemReservadoRepository.findByReservaId(reserva.getId());

        if (itemsReservados.isEmpty()) {
            return;
        }

        Map<Long, Integer> itemsToRelease = new HashMap<>();
        for (ItemReservado itemReservado : itemsReservados) {
            itemsToRelease.put(itemReservado.getItem().getId(), itemReservado.getCantidad());
        }

        // Use StockService to release stock with reserva information
        stockService.liberarStock(itemsToRelease, reserva);
    }

    /**
     * Calcular disponibilidad de un item en un período
     */
    public int calcularDisponibilidadItem(Long itemId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin) {
        ItemsInventario item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item no encontrado"));

        Integer cantidadReservada = itemReservadoRepository
                .countReservadasEnPeriodo(itemId, fechaInicio, fechaFin);

        return item.getCantidadTotal() - cantidadReservada;
    }

    /**
     * Obtener item por ID
     */
    public ItemsInventario obtenerPorId(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item no encontrado"));
    }

    /**
     * Obtener items por recurso
     */
    public List<ItemsInventario> obtenerItemsPorRecurso(Long recursoId) {
        return itemRepository.findByRecursoId(recursoId);
    }

    /**
     * Obtener todos los items del inventario
     */
    public List<ItemsInventario> obtenerTodos() {
        return itemRepository.findAll();
    }

    /**
     * Agregar nuevo item
     */
    public ItemsInventario agregarItem(ItemsInventario item) {
        return itemRepository.save(item);
    }

    /**
     * Agregar nuevo item con recurso asignado
     */
    public ItemsInventario agregarItemConRecurso(
            com.bms.reserva_servicio_backend.request.ItemInventarioRequest request) {
        // Buscar el recurso
        com.bms.reserva_servicio_backend.models.Recurso recurso = recursoRepository.findById(request.getRecursoId())
                .orElseThrow(
                        () -> new EntityNotFoundException("Recurso no encontrado con ID: " + request.getRecursoId()));

        // Crear el item
        ItemsInventario item = request.toEntity();
        item.setRecurso(recurso);

        return itemRepository.save(item);
    }

    /**
     * Actualizar item
     */
    public ItemsInventario actualizarItem(Long id, ItemsInventario itemActualizado) {
        ItemsInventario item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item no encontrado"));

        item.setNombre(itemActualizado.getNombre());
        item.setCategoria(itemActualizado.getCategoria());
        item.setCantidadTotal(itemActualizado.getCantidadTotal());
        item.setEstadoItem(itemActualizado.getEstadoItem());
        item.setEsReservable(itemActualizado.getEsReservable());
        item.setPrecioReserva(itemActualizado.getPrecioReserva());

        return itemRepository.save(item);
    }

    /**
     * Actualizar item con recurso asignado
     */
    public ItemsInventario actualizarItemConRecurso(Long id,
            com.bms.reserva_servicio_backend.request.ItemInventarioRequest request) {
        ItemsInventario item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item no encontrado con ID: " + id));

        // Buscar el recurso si cambió
        if (request.getRecursoId() != null) {
            com.bms.reserva_servicio_backend.models.Recurso recurso = recursoRepository.findById(request.getRecursoId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Recurso no encontrado con ID: " + request.getRecursoId()));
            item.setRecurso(recurso);
        }

        // Actualizar campos
        item.setNombre(request.getNombre());
        item.setCategoria(request.getCategoria());
        item.setCantidadTotal(request.getCantidadTotal());

        // Convertir String a Enum para estadoItem
        if (request.getEstadoItem() != null) {
            try {
                item.setEstadoItem(EstadoItem.valueOf(request.getEstadoItem().toUpperCase()));
            } catch (IllegalArgumentException e) {
                item.setEstadoItem(EstadoItem.BUENO);
            }
        } else {
            item.setEstadoItem(EstadoItem.BUENO);
        }

        item.setEsReservable(request.getEsReservable());
        item.setPrecioReserva(request.getPrecioReserva());

        // Mantener cantidadDisponible si no cambió el total
        if (!item.getCantidadTotal().equals(request.getCantidadTotal())) {
            // Si cambió el total, ajustar el disponible proporcionalmente
            item.setCantidadDisponible(request.getCantidadTotal());
        }

        return itemRepository.save(item);
    }

    /**
     * Eliminar item
     */
    public void eliminarItem(Long id) {
        ItemsInventario item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item no encontrado"));

        // Verificar que no tenga reservas activas
        List<ItemReservado> reservasActivas = itemReservadoRepository
                .findByItemIdAndReservaEstado(id, EstadoReserva.CONFIRMADA);

        if (!reservasActivas.isEmpty()) {
            throw new IllegalStateException("No se puede eliminar un item con reservas activas");
        }

        itemRepository.delete(item);
    }
}
