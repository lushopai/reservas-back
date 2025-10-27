package com.bms.reserva_servicio_backend.exception;

/**
 * Excepción lanzada cuando no hay suficiente stock/inventario para una reserva
 */
public class InsufficientStockException extends BusinessRuleException {

    private final Long itemId;
    private final Integer cantidadSolicitada;
    private final Integer cantidadDisponible;

    public InsufficientStockException(Long itemId, Integer cantidadSolicitada, Integer cantidadDisponible) {
        super(String.format("Stock insuficiente para item %d. Solicitado: %d, Disponible: %d",
                itemId, cantidadSolicitada, cantidadDisponible));
        this.itemId = itemId;
        this.cantidadSolicitada = cantidadSolicitada;
        this.cantidadDisponible = cantidadDisponible;
    }

    public Long getItemId() {
        return itemId;
    }

    public Integer getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public Integer getCantidadDisponible() {
        return cantidadDisponible;
    }
}
