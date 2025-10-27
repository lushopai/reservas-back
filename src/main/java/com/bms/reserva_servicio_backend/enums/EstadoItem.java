package com.bms.reserva_servicio_backend.enums;

/**
 * Estados posibles de un item del inventario
 */
public enum EstadoItem {
    /**
     * Item nuevo sin uso
     */
    NUEVO,

    /**
     * Item en buen estado
     */
    BUENO,

    /**
     * Item en estado regular, funcional pero con desgaste
     */
    REGULAR,

    /**
     * Item en reparación, no disponible
     */
    REPARACION;

    /**
     * Verifica si el item está disponible para uso
     */
    public boolean estaDisponible() {
        return this != REPARACION;
    }
}
