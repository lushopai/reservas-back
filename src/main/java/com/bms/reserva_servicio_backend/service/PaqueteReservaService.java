package com.bms.reserva_servicio_backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bms.reserva_servicio_backend.dto.PagoDTO;
import com.bms.reserva_servicio_backend.dto.PaqueteReservaDTO;
import com.bms.reserva_servicio_backend.dto.ServicioReservaDTO;
import com.bms.reserva_servicio_backend.enums.EstadoPaquete;
import com.bms.reserva_servicio_backend.enums.EstadoReserva;
import com.bms.reserva_servicio_backend.models.PaqueteReserva;
import com.bms.reserva_servicio_backend.models.Reserva;
import com.bms.reserva_servicio_backend.repository.PaqueteReservaRepository;
import com.bms.reserva_servicio_backend.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class PaqueteReservaService {

    @Autowired
    private PaqueteReservaRepository paqueteRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private UserRepository userRepository; // CAMBIADO: ClienteRepository -> UserRepository

    @Autowired
    private PagoService pagoService;

    /**
     * Crear paquete combinado: Cabaña + Servicios
     * 
     * @throws Exception
     */
    public PaqueteReserva crearPaqueteCompleto(Long userId,
            PaqueteReservaDTO paqueteDTO) throws Exception {

        PaqueteReserva paquete = new PaqueteReserva();
        paquete.setUser(userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Usuario no encontrado"))); // CAMBIADO: setCliente -> setUser
        paquete.setNombrePaquete(paqueteDTO.getNombre());
        paquete.setFechaCreacion(LocalDateTime.now()); // Establecer fecha de creación
        paquete.setFechaInicio(paqueteDTO.getFechaInicio());
        paquete.setFechaFin(paqueteDTO.getFechaFin());
        paquete.setEstado(EstadoPaquete.BORRADOR); // Estado inicial para permitir modificaciones antes del pago
        paquete.setNotasEspeciales(paqueteDTO.getNotasEspeciales());

        // IMPORTANTE: Inicializar precios en cero para cumplir con validaciones @NotNull
        // Se calcularán correctamente después de crear las reservas
        paquete.setPrecioTotal(BigDecimal.ZERO);
        paquete.setDescuento(BigDecimal.ZERO);
        paquete.setPrecioFinal(BigDecimal.ZERO);

        // Guardar paquete inicial con precios en cero
        paquete = paqueteRepository.save(paquete);

        BigDecimal precioTotal = BigDecimal.ZERO;

        // Agregar reserva de cabaña
        if (paqueteDTO.getCabanaId() != null) {
            Reserva reservaCabana = reservaService.reservarCabaña(
                    paqueteDTO.getCabanaId(),
                    userId,
                    paqueteDTO.getFechaInicio().toLocalDate(),
                    paqueteDTO.getFechaFin().toLocalDate(),
                    paqueteDTO.getItemsCabana());
            reservaCabana.setPaquete(paquete);
            precioTotal = precioTotal.add(reservaCabana.getPrecioTotal());
        }

        // Agregar reservas de servicios
        if (paqueteDTO.getServicios() != null) {
            for (ServicioReservaDTO servicioDTO : paqueteDTO.getServicios()) {
                Reserva reservaServicio = reservaService.reservarServicio(
                        servicioDTO.getServicioId(),
                        userId,
                        servicioDTO.getFecha(),
                        servicioDTO.getHoraInicio(),
                        servicioDTO.getDuracionBloques(),
                        servicioDTO.getEquipamiento());
                reservaServicio.setPaquete(paquete);
                precioTotal = precioTotal.add(reservaServicio.getPrecioTotal());
            }
        }

        // Actualizar con los precios calculados reales
        paquete.setPrecioTotal(precioTotal);
        paquete.setDescuento(BigDecimal.ZERO);
        paquete.setPrecioFinal(precioTotal);

        // Cambiar estado a PENDIENTE ya que todas las reservas fueron creadas exitosamente
        paquete.setEstado(EstadoPaquete.PENDIENTE);

        return paqueteRepository.save(paquete);
    }

    /**
     * Confirmar todo el paquete
     */
    public PaqueteReserva confirmarPaquete(Long paqueteId, PagoDTO pagoDTO) {
        PaqueteReserva paquete = paqueteRepository.findById(paqueteId)
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado"));

        // Procesar pago del paquete completo
        pagoService.procesarPagoPaquete(paquete, pagoDTO);

        // Confirmar todas las reservas del paquete
        for (Reserva reserva : paquete.getReservas()) {
            reserva.setEstado(EstadoReserva.CONFIRMADA);
        }

        paquete.setEstado(EstadoPaquete.ACTIVO);
        return paqueteRepository.save(paquete);
    }

    /**
     * Obtener paquete por ID
     */
    public PaqueteReserva obtenerPorId(Long id) {
        return paqueteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Paquete no encontrado"));
    }

    /**
     * Obtener paquetes por cliente
     */
    public List<PaqueteReserva> obtenerPorCliente(Long userId) {
        return paqueteRepository.findByUserId(userId);
    }

    /**
     * Obtener paquetes por estado
     */
    public List<PaqueteReserva> obtenerPorEstado(EstadoPaquete estado) {
        return paqueteRepository.findByEstado(estado);
    }

    /**
     * Modificar paquete en borrador
     */
    public PaqueteReserva modificarPaquete(Long id, PaqueteReservaDTO dto) {
        PaqueteReserva paquete = obtenerPorId(id);

        if (paquete.getEstado() != EstadoPaquete.BORRADOR) {
            throw new IllegalStateException("Solo se pueden modificar paquetes en borrador");
        }

        // Actualizar datos
        paquete.setNombrePaquete(dto.getNombre());
        paquete.setFechaInicio(dto.getFechaInicio());
        paquete.setFechaFin(dto.getFechaFin());
        paquete.setNotasEspeciales(dto.getNotasEspeciales());

        // Recalcular precios si es necesario
        // ...

        return paqueteRepository.save(paquete);
    }

}
