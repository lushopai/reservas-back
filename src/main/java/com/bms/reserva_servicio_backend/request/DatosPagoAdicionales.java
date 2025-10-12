package com.bms.reserva_servicio_backend.request;

import lombok.Data;

/**
 * Datos adicionales del pago
 */
@Data
public class DatosPagoAdicionales {
    private String numeroTarjeta;      // Últimos 4 dígitos
    private String tipoTarjeta;        // Visa, Mastercard
    private String nombreTitular;
    private String emailComprobante;
}