package com.bms.reserva_servicio_backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bms.reserva_servicio_backend.dto.ItemReservaDTO;
import com.bms.reserva_servicio_backend.dto.PagoDTO;
import com.bms.reserva_servicio_backend.models.Cabana;
import com.bms.reserva_servicio_backend.models.ItemsInventario;
import com.bms.reserva_servicio_backend.models.Reserva;
import com.bms.reserva_servicio_backend.models.ServicioEntretencion;
import com.bms.reserva_servicio_backend.models.User;
import com.bms.reserva_servicio_backend.repository.CabanaRepository;
import com.bms.reserva_servicio_backend.repository.ReservaRepository;
import com.bms.reserva_servicio_backend.repository.ServicioEntretencionRepository;
import com.bms.reserva_servicio_backend.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private CabanaRepository cabanaRepository;

    @Autowired
    private ServicioEntretencionRepository servicioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DisponibilidadService disponibilidadService;

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private PrecioService precioService;

    @Autowired
    private ValidacionService validacionService;

    @Autowired
    private PagoService pagoService;

    /**
     * Reservar cabaña por dias
     * 
     * @throws Exception
     */

    public Reserva reservarCabaña(Long cabanaId, Long userId,
            LocalDate fechaInicio, LocalDate fechaFin, List<ItemReservaDTO> itemsAdicionales) throws Exception {

        // Validar reglas de negocio
        validacionService.validarReservaCabana(fechaInicio, fechaFin, null);

        // Obtener cabaña
        Cabana cabana = cabanaRepository.findById(cabanaId)
                .orElseThrow(() -> new EntityNotFoundException("Cabaña no encontrada"));

        // Obtener usuario (antes cliente)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        // Verificar disponibilidad

        if (!disponibilidadService.validarDisponibilidadCabana(cabanaId, fechaInicio, fechaFin)) {
            throw new Exception("Cabaña no disponible en las fechas seleccionadas");
        }

        // Validar items del inventario
        if (itemsAdicionales != null && !itemsAdicionales.isEmpty()) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(12, 0);
            inventarioService.validarDisponibilidadItems(itemsAdicionales, inicio, fin);
        }

        // Calcular precios
        BigDecimal precioBase = precioService.calcularPrecioCabana(cabana, fechaInicio, fechaFin);
        BigDecimal precioItems = calcularPrecioItems(itemsAdicionales);

        // Crear reserva

        Reserva reserva = new Reserva();
        reserva.setRecurso(cabana);
        reserva.setUser(user);  // CAMBIADO: setCliente -> setUser
        reserva.setFechaFin(LocalDateTime.now());
        reserva.setFechaInicio(fechaInicio.atTime(15, 0));
        reserva.setFechaFin(fechaFin.atTime(12, 0));
        reserva.setPrecioBase(precioBase);
        reserva.setPrecioItems(precioItems);
        reserva.setPrecioTotal(precioBase.add(precioItems));
        reserva.setEstado("PENDIENTE");
        reserva.setTipoReserva("CABAÑA_DIA");

        reserva = reservaRepository.save(reserva);

        // Reservar items si existen
        if (itemsAdicionales != null && !itemsAdicionales.isEmpty()) {
            inventarioService.reservarItems(reserva, itemsAdicionales);
        }
        // Bloquear disponibilidad
        disponibilidadService.bloquearFechasCabana(cabanaId, fechaInicio, fechaFin);
        return reserva;

    }

    /**
     * Reservar servicio por bloques horarios
     * 
     * @throws Exception
     */
    public Reserva reservarServicio(Long servicioId, Long userId, LocalDate fecha,
            LocalTime horaInicio, Integer duracionBloques, List<ItemReservaDTO> equipamiento) throws Exception {

        // Validart reglas de negocio
        validacionService.validarReservaServicio(fecha, horaInicio, duracionBloques);

        ServicioEntretencion servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        // Calcular hora fin
        LocalTime horaFin = horaInicio.plusMinutes(
                servicio.getDuracionBloqueMinutos() * duracionBloques);

        // Verificar disponibilidad del bloque horario
        if (!disponibilidadService.validarDisponibilidadServicio(servicioId, fecha, horaInicio, horaFin)) {
            throw new Exception("Servicio no disponible en el horario solicitado");
        }
        // Validar equipamiento disponible
        if (equipamiento != null && !equipamiento.isEmpty()) {
            LocalDateTime inicio = fecha.atTime(horaInicio);
            LocalDateTime fin = fecha.atTime(horaFin);
            inventarioService.validarDisponibilidadItems(equipamiento, inicio, fin);
        }
        // Calcular precios
        BigDecimal precioBase = precioService.calcularPrecioServicio(servicio, duracionBloques);
        BigDecimal precioEquipamiento = calcularPrecioItems(equipamiento);

        // Crear reserva
        Reserva reserva = new Reserva();
        reserva.setRecurso(servicio);
        reserva.setUser(user);  // CAMBIADO: setCliente -> setUser
        reserva.setFechaReserva(LocalDateTime.now());
        reserva.setFechaInicio(fecha.atTime(horaInicio));
        reserva.setFechaFin(fecha.atTime(horaFin));
        reserva.setPrecioBase(precioBase);
        reserva.setPrecioItems(precioEquipamiento);
        reserva.setPrecioTotal(precioBase.add(precioEquipamiento));
        reserva.setEstado("PENDIENTE");
        reserva.setTipoReserva("SERVICIO_BLOQUE");

        reserva = reservaRepository.save(reserva);

        // Reservar equipamiento
        if (equipamiento != null && !equipamiento.isEmpty()) {
            inventarioService.reservarItems(reserva, equipamiento);
        }

        // Bloquear horario
        disponibilidadService.bloquearBloqueHorario(servicioId, fecha, horaInicio, horaFin);

        return reserva;
    }

    /**
     * Confirmar reserva y procesar pago
     */
    public Reserva confirmarReserva(Long reservaId, PagoDTO pagoDTO) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada"));

        if (!"PENDIENTE".equals(reserva.getEstado())) {
            throw new IllegalStateException("La reserva no está en estado pendiente");
        }

        // Validar monto
        if (!pagoDTO.getMonto().equals(reserva.getPrecioTotal())) {
            throw new IllegalArgumentException("El monto del pago no coincide con el total");
        }

        // Procesar pago
        pagoService.procesarPago(reserva, pagoDTO);

        // Actualizar estado
        reserva.setEstado("CONFIRMADA");
        return reservaRepository.save(reserva);
    }

    /**
     * Cancelar reserva
     */
    public void cancelarReserva(Long reservaId, String motivo) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada"));

        // Liberar inventario
        inventarioService.liberarItems(reserva);

        // Liberar disponibilidad
        disponibilidadService.liberarRecurso(reserva);

        // Actualizar estado
        reserva.setEstado("CANCELADA");
        reserva.setObservaciones(motivo);
        reservaRepository.save(reserva);
    }

    /**
     * Obtener reservas por cliente
     */
    public List<Reserva> obtenerReservasPorCliente(Long userId) {
        return reservaRepository.findByUserId(userId);
    }

    /**
     * Obtener reserva por ID
     */
    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada"));
    }

    /**
     * Obtener reservas por estado
     */
    public List<Reserva> obtenerPorEstado(String estado) {
        return reservaRepository.findByEstado(estado);
    }

    /**
     * Calcular precio de items
     */
    private BigDecimal calcularPrecioItems(List<ItemReservaDTO> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (ItemReservaDTO itemDTO : items) {
            ItemsInventario item = inventarioService.obtenerPorId(itemDTO.getItemId());
            if (item.getPrecioReserva() != null) {
                BigDecimal subtotal = item.getPrecioReserva()
                        .multiply(new BigDecimal(itemDTO.getCantidad()));
                total = total.add(subtotal);
            }
        }
        return total;
    }

}
