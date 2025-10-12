package com.bms.reserva_servicio_backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bms.reserva_servicio_backend.dto.PagoDTO;
import com.bms.reserva_servicio_backend.models.Pagos;
import com.bms.reserva_servicio_backend.models.PaqueteReserva;
import com.bms.reserva_servicio_backend.models.Reserva;
import com.bms.reserva_servicio_backend.repository.PagoRepository;

@Service
@Transactional
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    /**
     * Procesar pago de una reserva individual
     */
    public Pagos procesarPago(Reserva reserva, PagoDTO pagoDTO) {
        Pagos pago = new Pagos();
        pago.setReserva(reserva);
        pago.setMonto(pagoDTO.getMonto());
        pago.setMetodoPago(pagoDTO.getMetodoPago());
        pago.setTransaccionId(pagoDTO.getTransaccionId());
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstado("COMPLETADO");
        
        return pagoRepository.save(pago);
    }

     /**
     * Procesar pago de un paquete completo
     */
    public Pagos procesarPagoPaquete(PaqueteReserva paquete, PagoDTO pagoDTO) {
        Pagos pago = new Pagos();
        pago.setPaquete(paquete);
        pago.setMonto(pagoDTO.getMonto());
        pago.setMetodoPago(pagoDTO.getMetodoPago());
        pago.setTransaccionId(pagoDTO.getTransaccionId());
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstado("COMPLETADO");
        
        return pagoRepository.save(pago);
    }
    
    /**
     * Procesar reembolso de paquete
     */
    public Pagos procesarReembolsoPaquete(PaqueteReserva paquete) {
        // Calcular monto de reembolso según política
        BigDecimal montoReembolso = calcularMontoReembolso(paquete);
        
        Pagos reembolso = new Pagos();
        reembolso.setPaquete(paquete);
        reembolso.setMonto(montoReembolso.negate()); // Negativo para reembolso
        reembolso.setMetodoPago("REEMBOLSO");
        reembolso.setFechaPago(LocalDateTime.now());
        reembolso.setEstado("COMPLETADO");
        
        return pagoRepository.save(reembolso);
    }
    
    /**
     * Calcular monto de reembolso
     */
    private BigDecimal calcularMontoReembolso(PaqueteReserva paquete) {
        long diasAntes = ChronoUnit.DAYS.between(
            LocalDateTime.now(),
            paquete.getFechaInicio()
        );
        
        BigDecimal montoPagado = paquete.getPrecioFinal();
        
        if (diasAntes >= 30) {
            return montoPagado; // 100%
        } else if (diasAntes >= 15) {
            return montoPagado.multiply(new BigDecimal("0.80")); // 80%
        } else if (diasAntes >= 7) {
            return montoPagado.multiply(new BigDecimal("0.50")); // 50%
        } else if (diasAntes >= 3) {
            return montoPagado.multiply(new BigDecimal("0.25")); // 25%
        } else {
            return BigDecimal.ZERO; // Sin reembolso
        }
    }
    
    /**
     * Obtener pagos de una reserva
     */
    public List<Pagos> obtenerPagosPorReserva(Long reservaId) {
        return pagoRepository.findByReservaId(reservaId);
    }
    
    /**
     * Obtener pagos de un paquete
     */
    public List<Pagos> obtenerPagosPorPaquete(Long paqueteId) {
        return pagoRepository.findByPaqueteId(paqueteId);
    }
}
