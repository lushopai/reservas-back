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
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final CabanaRepository cabanaRepository;
    private final ServicioEntretencionRepository servicioRepository;
    private final UserRepository userRepository;
    private final DisponibilidadService disponibilidadService;
    private final InventarioService inventarioService;
    private final PrecioService precioService;
    private final ValidacionService validacionService;
    private final PagoService pagoService;

    @Autowired
    public ReservaService(ReservaRepository reservaRepository, CabanaRepository cabanaRepository,
            ServicioEntretencionRepository servicioRepository, UserRepository userRepository,
            DisponibilidadService disponibilidadService, InventarioService inventarioService,
            PrecioService precioService, ValidacionService validacionService, PagoService pagoService) {
        this.reservaRepository = reservaRepository;
        this.cabanaRepository = cabanaRepository;
        this.servicioRepository = servicioRepository;
        this.userRepository = userRepository;
        this.disponibilidadService = disponibilidadService;
        this.inventarioService = inventarioService;
        this.precioService = precioService;
        this.validacionService = validacionService;
        this.pagoService = pagoService;
    }

    /**
     * Reservar cabaña por dias
     * 
     * @throws Exception
     */

    public Reserva reservarCabaña(Long cabanaId, Long userId,
            LocalDate fechaInicio, LocalDate fechaFin, List<ItemReservaDTO> itemsAdicionales) {

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
            throw new IllegalStateException("Cabaña no disponible en las fechas seleccionadas");
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
        reserva.setFechaReserva(LocalDateTime.now());  // CORREGIDO: era setFechaFin
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
            LocalTime horaInicio, Integer duracionBloques, List<ItemReservaDTO> equipamiento) {

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
            throw new IllegalStateException("Servicio no disponible en el horario solicitado");
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
     * Obtener todas las reservas
     */
    public List<Reserva> obtenerTodas() {
        return reservaRepository.findAll();
    }

    /**
     * Cambiar estado de una reserva con validaciones
     */
    public Reserva cambiarEstado(Long reservaId, String nuevoEstado, String motivo, String observaciones) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva no encontrada"));

        String estadoActual = reserva.getEstado();

        // Validar transición de estado
        if (!esTransicionValida(estadoActual, nuevoEstado)) {
            throw new IllegalStateException(
                String.format("No se puede cambiar de %s a %s", estadoActual, nuevoEstado)
            );
        }

        // Aplicar cambio de estado
        reserva.setEstado(nuevoEstado);

        // Agregar observaciones si existen
        if (observaciones != null && !observaciones.trim().isEmpty()) {
            String obsActuales = reserva.getObservaciones() != null ? reserva.getObservaciones() : "";
            reserva.setObservaciones(obsActuales + "\n" + LocalDateTime.now() + " - " + observaciones);
        }

        // Si se cancela o completa, liberar recursos e inventario
        if ("CANCELADA".equals(nuevoEstado)) {
            inventarioService.liberarItems(reserva);
            disponibilidadService.liberarRecurso(reserva);

            if (motivo != null) {
                String obsActuales = reserva.getObservaciones() != null ? reserva.getObservaciones() : "";
                reserva.setObservaciones(obsActuales + "\nMotivo cancelación: " + motivo);
            }
        } else if ("COMPLETADA".equals(nuevoEstado)) {
            // ✅ Al completar, también liberar items para que vuelvan a estar disponibles
            inventarioService.liberarItems(reserva);
            // No liberamos el recurso porque ya se usó, solo los items
        }

        return reservaRepository.save(reserva);
    }

    /**
     * Validar si una transición de estado es válida
     */
    private boolean esTransicionValida(String estadoActual, String nuevoEstado) {
        // Estados válidos
        if (!List.of("BORRADOR", "PENDIENTE", "CONFIRMADA", "EN_CURSO", "COMPLETADA", "CANCELADA")
                .contains(nuevoEstado)) {
            return false;
        }

        // Desde CANCELADA o COMPLETADA no se puede cambiar
        if ("CANCELADA".equals(estadoActual) || "COMPLETADA".equals(estadoActual)) {
            return false;
        }

        // Transiciones válidas según flujo de negocio
        switch (estadoActual) {
            case "BORRADOR":
                return List.of("PENDIENTE", "CANCELADA").contains(nuevoEstado);
            case "PENDIENTE":
                return List.of("CONFIRMADA", "CANCELADA").contains(nuevoEstado);
            case "CONFIRMADA":
                return List.of("EN_CURSO", "CANCELADA").contains(nuevoEstado);
            case "EN_CURSO":
                return List.of("COMPLETADA", "CANCELADA").contains(nuevoEstado);
            default:
                return false;
        }
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
