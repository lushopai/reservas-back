package com.bms.reserva_servicio_backend.exception;

/**
 * Excepción lanzada cuando se viola una regla de negocio
 * Ejemplos:
 * - Intentar reservar una cabaña que no está disponible
 * - Cancelar una reserva que ya fue completada
 * - Cambiar estado de forma inválida
 */
public class BusinessRuleException extends RuntimeException {

    private final String errorCode;

    public BusinessRuleException(String message) {
        super(message);
        this.errorCode = null;
    }

    public BusinessRuleException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
