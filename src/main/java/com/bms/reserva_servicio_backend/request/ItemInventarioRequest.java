package com.bms.reserva_servicio_backend.request;

import java.math.BigDecimal;

import com.bms.reserva_servicio_backend.enums.EstadoItem;
import com.bms.reserva_servicio_backend.models.ItemsInventario;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * REQUEST para crear/actualizar item de inventario
 * POST /api/inventario
 * PUT /api/inventario/{id}
 */
@Data
public class ItemInventarioRequest {

    @NotNull(message = "El ID del recurso es obligatorio")
    private Long recursoId;

    @NotBlank(message = "El nombre del item es obligatorio")
    private String nombre;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    @NotNull(message = "La cantidad total es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer cantidadTotal;

    private String estadoItem; // Se mantiene como String para el DTO, se convierte a Enum en toEntity()

    @NotNull(message = "Debe indicar si es reservable")
    private Boolean esReservable;

    private BigDecimal precioReserva;

    /**
     * Convertir a entidad
     */
    public ItemsInventario toEntity() {
        ItemsInventario item = new ItemsInventario();
        item.setNombre(this.nombre);
        item.setCategoria(this.categoria);
        item.setCantidadTotal(this.cantidadTotal);
        item.setCantidadDisponible(this.cantidadTotal); // Inicialmente todo disponible

        // Convertir String a Enum
        if (this.estadoItem != null) {
            try {
                item.setEstadoItem(EstadoItem.valueOf(this.estadoItem.toUpperCase()));
            } catch (IllegalArgumentException e) {
                item.setEstadoItem(EstadoItem.BUENO); // Valor por defecto si es inválido
            }
        } else {
            item.setEstadoItem(EstadoItem.BUENO);
        }

        item.setEsReservable(this.esReservable);
        item.setPrecioReserva(this.precioReserva);
        return item;
    }
}
