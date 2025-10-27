package com.bms.reserva_servicio_backend.enums;

/**
 * Tipos de reserva disponibles en el sistema
 */
public enum TipoReserva {
    /**
     * Reserva de cabaña por días completos
     */
    CABANA_DIA,

    /**
     * Reserva de servicio por bloques horarios
     */
    SERVICIO_BLOQUE,

    /**
     * Reserva como parte de un paquete
     */
    PAQUETE
}
