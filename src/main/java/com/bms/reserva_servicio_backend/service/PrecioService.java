package com.bms.reserva_servicio_backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.bms.reserva_servicio_backend.models.Cabana;
import com.bms.reserva_servicio_backend.models.ServicioEntretencion;

@Service
public class PrecioService {

    /**
     * Calcular precio total de una cabaña considerando:
     * - Número de días
     * - Precio base por noche
     * - Temporada (alta, media, baja)
     * - Descuentos por estadía prolongada
     */
    public BigDecimal calcularPrecioCabana(Cabana cabana,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        // 1. Calcular número de días
        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        if (dias <= 0) {
            throw new IllegalArgumentException("Las fechas son inválidas");
        }

        // 2. Precio base por noche
        BigDecimal precioPorNoche = cabana.getPrecioPorUnidad();

        // 3. Precio sin descuentos ni multiplicadores
        BigDecimal precioBase = precioPorNoche.multiply(new BigDecimal(dias));

        // 4. Aplicar multiplicador de temporada
        BigDecimal multiplicadorTemporada = calcularMultiplicadorTemporada(fechaInicio, fechaFin);
        BigDecimal precioConTemporada = precioBase.multiply(multiplicadorTemporada);

        // 5. Aplicar descuento por estadía prolongada
        BigDecimal precioFinal = aplicarDescuentoPorEstadia(precioConTemporada, (int) dias);

        return precioFinal;
    }

    /**
     * Calcular precio de servicio por bloques horarios
     */
    public BigDecimal calcularPrecioServicio(ServicioEntretencion servicio,
            Integer duracionBloques) {

        if (duracionBloques <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a 0");
        }

        // Precio por bloque (puede ser por hora)
        BigDecimal precioPorBloque = servicio.getPrecioPorUnidad();

        // Multiplicar por cantidad de bloques
        BigDecimal precioTotal = precioPorBloque.multiply(new BigDecimal(duracionBloques));

        // Opcional: Aplicar descuento por reservas largas
        if (duracionBloques >= 4) {
            // 5% descuento si reserva 4+ horas
            precioTotal = precioTotal.multiply(new BigDecimal("0.95"));
        }

        return precioTotal;
    }

    /**
     * Calcular multiplicador según temporada
     * 
     * TEMPORADAS en Chile:
     * - Alta: Diciembre-Febrero (Verano) + Julio (Vacaciones invierno) = 1.3x
     * - Media: Marzo-Junio, Agosto-Noviembre = 1.0x
     * - Baja: No aplicamos baja en este ejemplo, pero podrías agregar = 0.8x
     */
    private BigDecimal calcularMultiplicadorTemporada(LocalDate inicio, LocalDate fin) {

        // Simplificado: tomamos el mes de inicio para determinar temporada
        // int mes = inicio.getMonthValue();

        // Temporada ALTA (DESHABILITADA POR AHORA)
        // if (mes == 12 || mes == 1 || mes == 2 || mes == 7) {
        // return new BigDecimal("1.30"); // +30%
        // }

        // Temporada MEDIA (resto del año)
        return new BigDecimal("1.00"); // Precio normal

        // Si quieres temporada BAJA:
        // if (mes == 4 || mes == 5 || mes == 9 || mes == 10) {
        // return new BigDecimal("0.80"); // -20%
        // }
    }

    /**
     * Aplicar descuentos por estadía prolongada
     * 
     * POLÍTICA DE DESCUENTOS:
     * - 14+ días: 20% descuento
     * - 7-13 días: 15% descuento
     * - 4-6 días: 10% descuento
     * - 1-3 días: Sin descuento
     */
    private BigDecimal aplicarDescuentoPorEstadia(BigDecimal precio, int dias) {

        if (dias >= 14) {
            // 20% descuento
            return precio.multiply(new BigDecimal("0.80"));
        } else if (dias >= 7) {
            // 15% descuento
            return precio.multiply(new BigDecimal("0.85"));
        } else if (dias >= 4) {
            // 10% descuento
            return precio.multiply(new BigDecimal("0.90"));
        }

        // Sin descuento para estadías cortas
        return precio;
    }

    /**
     * Calcular descuento para paquetes
     */
    public BigDecimal calcularDescuentoPaquete(BigDecimal precioTotal,
            int cantidadServicios,
            int diasEstadia) {

        // Descuento base por armar paquete
        BigDecimal porcentajeDescuento = new BigDecimal("0.10"); // 10%

        // Descuento adicional si incluye muchos servicios
        if (cantidadServicios >= 4) {
            porcentajeDescuento = new BigDecimal("0.15"); // 15%
        } else if (cantidadServicios >= 3) {
            porcentajeDescuento = new BigDecimal("0.12"); // 12%
        }

        // Descuento adicional por estadía larga
        if (diasEstadia >= 7) {
            porcentajeDescuento = porcentajeDescuento.add(new BigDecimal("0.05")); // +5%
        }

        // Calcular monto del descuento
        return precioTotal.multiply(porcentajeDescuento);
    }

    /**
     * Calcular precio especial para clientes VIP
     */
    public BigDecimal aplicarDescuentoVIP(BigDecimal precio, boolean esVIP) {
        if (esVIP) {
            // 5% descuento adicional para VIPs
            return precio.multiply(new BigDecimal("0.95"));
        }
        return precio;
    }

    /**
     * Calcular precio con impuestos (si aplica)
     */
    public BigDecimal calcularPrecioConImpuestos(BigDecimal precioBase) {
        // IVA 19% en Chile
        BigDecimal iva = new BigDecimal("0.19");
        BigDecimal montoIva = precioBase.multiply(iva);
        return precioBase.add(montoIva);
    }

}
