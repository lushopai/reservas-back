package com.bms.reserva_servicio_backend.exception;

import com.bms.reserva_servicio_backend.enums.EstadoReserva;

/**
 * Excepción lanzada cuando se intenta realizar una transición de estado inválida
 */
public class InvalidStateTransitionException extends BusinessRuleException {

    private final EstadoReserva estadoActual;
    private final EstadoReserva estadoDestino;

    public InvalidStateTransitionException(EstadoReserva estadoActual, EstadoReserva estadoDestino) {
        super(String.format("No se puede cambiar del estado %s a %s", estadoActual, estadoDestino));
        this.estadoActual = estadoActual;
        this.estadoDestino = estadoDestino;
    }

    public EstadoReserva getEstadoActual() {
        return estadoActual;
    }

    public EstadoReserva getEstadoDestino() {
        return estadoDestino;
    }
}
