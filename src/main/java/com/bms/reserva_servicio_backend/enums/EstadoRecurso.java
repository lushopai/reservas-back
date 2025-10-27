package com.bms.reserva_servicio_backend.enums;

/**
 * Estados posibles de un recurso (Cabaña o Servicio)
 */
public enum EstadoRecurso {
    /**
     * Recurso disponible para reservas
     */
    DISPONIBLE,

    /**
     * Recurso en mantenimiento, no disponible temporalmente
     */
    MANTENIMIENTO,

    /**
     * Recurso fuera de servicio permanentemente
     */
    FUERA_SERVICIO;

    /**
     * Verifica si el recurso puede ser reservado
     */
    public boolean esReservable() {
        return this == DISPONIBLE;
    }
}
