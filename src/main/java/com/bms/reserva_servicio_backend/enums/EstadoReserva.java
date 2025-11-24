package com.bms.reserva_servicio_backend.enums;

/**
 * Estados posibles de una reserva en su ciclo de vida
 */
public enum EstadoReserva {
    BORRADOR,
    PENDIENTE_PAGO, // Nuevo estado: Reserva creada, esperando pago
    PENDIENTE,      // Estado intermedio si la confirmación del pago no es instantánea
    CONFIRMADA,     // Pago recibido, reserva confirmada, esperando check-in
    EN_CURSO,       // Cliente ha hecho check-in, recurso en uso
    COMPLETADA,
    CANCELADA;

    /**
     * Verifica si una transición de estado es válida
     */
    public boolean puedeTransicionarA(EstadoReserva nuevoEstado) {
        if (nuevoEstado == null) {
            return false;
        }

        // Desde CANCELADA o COMPLETADA no se puede cambiar
        if (this == CANCELADA || this == COMPLETADA) {
            return false;
        }

        // Transiciones válidas según flujo de negocio
        switch (this) {
            case BORRADOR:
                return nuevoEstado == PENDIENTE_PAGO || nuevoEstado == CANCELADA;
            case PENDIENTE_PAGO:
                // Puede pasar a PENDIENTE (si hay un proceso de pago asíncrono) o CONFIRMADA (pago exitoso) o CANCELADA
                return nuevoEstado == PENDIENTE || nuevoEstado == CONFIRMADA || nuevoEstado == CANCELADA;
            case PENDIENTE:
                return nuevoEstado == CONFIRMADA || nuevoEstado == CANCELADA;
            case CONFIRMADA:
                return nuevoEstado == EN_CURSO || nuevoEstado == CANCELADA;
            case EN_CURSO:
                return nuevoEstado == COMPLETADA || nuevoEstado == CANCELADA;
            default:
                return false;
        }
    }

    /**
     * Verifica si el estado es terminal (no permite más cambios)
     */
    public boolean esEstadoTerminal() {
        return this == CANCELADA || this == COMPLETADA;
    }

    /**
     * Verifica si el estado es activo (reserva vigente)
     */
    public boolean esEstadoActivo() {
        return this == CONFIRMADA || this == EN_CURSO;
    }
}
