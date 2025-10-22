package com.bms.reserva_servicio_backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bms.reserva_servicio_backend.dto.ItemReservaDTO;
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

        for (ItemReservaDTO itemDTO : items) {
            ItemsInventario item = itemRepository.findById(itemDTO.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Item no encontrado"));

            ItemReservado itemReservado = new ItemReservado();
            itemReservado.setReserva(reserva);
            itemReservado.setItem(item);
            itemReservado.setCantidad(itemDTO.getCantidad());
            itemReservado.setPrecioUnitario(item.getPrecioReserva() != null ?
                item.getPrecioReserva() : BigDecimal.ZERO);
            itemReservado.setSubtotal(itemReservado.getPrecioUnitario()
                .multiply(new BigDecimal(itemDTO.getCantidad())));

            itemReservadoRepository.save(itemReservado);

            // Actualizar cantidad disponible en tiempo real
            item.setCantidadDisponible(item.getCantidadDisponible() - itemDTO.getCantidad());
            itemRepository.save(item);
        }
    }
    
    /**
     * Liberar items - Reponer stock al cancelar o finalizar reserva
     */
    public void liberarItems(Reserva reserva) {
        // Obtener items reservados de esta reserva
        List<ItemReservado> itemsReservados = itemReservadoRepository.findByReservaId(reserva.getId());

        for (ItemReservado itemReservado : itemsReservados) {
            ItemsInventario item = itemReservado.getItem();

            // Reponer cantidad disponible
            item.setCantidadDisponible(item.getCantidadDisponible() + itemReservado.getCantidad());
            itemRepository.save(item);
        }
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
    public ItemsInventario agregarItemConRecurso(com.bms.reserva_servicio_backend.request.ItemInventarioRequest request) {
        // Buscar el recurso
        com.bms.reserva_servicio_backend.models.Recurso recurso = recursoRepository.findById(request.getRecursoId())
            .orElseThrow(() -> new EntityNotFoundException("Recurso no encontrado con ID: " + request.getRecursoId()));

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
    public ItemsInventario actualizarItemConRecurso(Long id, com.bms.reserva_servicio_backend.request.ItemInventarioRequest request) {
        ItemsInventario item = itemRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Item no encontrado con ID: " + id));

        // Buscar el recurso si cambió
        if (request.getRecursoId() != null) {
            com.bms.reserva_servicio_backend.models.Recurso recurso = recursoRepository.findById(request.getRecursoId())
                .orElseThrow(() -> new EntityNotFoundException("Recurso no encontrado con ID: " + request.getRecursoId()));
            item.setRecurso(recurso);
        }

        // Actualizar campos
        item.setNombre(request.getNombre());
        item.setCategoria(request.getCategoria());
        item.setCantidadTotal(request.getCantidadTotal());
        item.setEstadoItem(request.getEstadoItem() != null ? request.getEstadoItem() : "BUENO");
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
            .findByItemIdAndReservaEstado(id, "CONFIRMADA");
        
        if (!reservasActivas.isEmpty()) {
            throw new IllegalStateException("No se puede eliminar un item con reservas activas");
        }
        
        itemRepository.delete(item);
    }
}
