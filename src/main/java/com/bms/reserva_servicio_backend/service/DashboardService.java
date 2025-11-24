package com.bms.reserva_servicio_backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bms.reserva_servicio_backend.enums.EstadoReserva;
import com.bms.reserva_servicio_backend.models.Cabana;
import com.bms.reserva_servicio_backend.models.Pagos;
import com.bms.reserva_servicio_backend.models.Reserva;
import com.bms.reserva_servicio_backend.models.User;
import com.bms.reserva_servicio_backend.repository.CabanaRepository;
import com.bms.reserva_servicio_backend.repository.PagoRepository;
import com.bms.reserva_servicio_backend.repository.ReservaRepository;
import com.bms.reserva_servicio_backend.repository.ServicioEntretencionRepository;
import com.bms.reserva_servicio_backend.repository.UserRepository;
import com.bms.reserva_servicio_backend.response.DashboardStatsResponse;
import com.bms.reserva_servicio_backend.response.DashboardStatsResponse.IngresosPorMesDTO;
import com.bms.reserva_servicio_backend.response.DashboardStatsResponse.ReservaResumeResponse;
import com.bms.reserva_servicio_backend.response.DashboardStatsResponse.ReservasPorDiaDTO;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private CabanaRepository cabanaRepository;

    @Autowired
    private ServicioEntretencionRepository servicioRepository;

    @Autowired
    private PagoRepository pagoRepository;

    public DashboardStatsResponse obtenerEstadisticas() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        // Estadísticas de usuarios
        stats.setTotalUsuarios(userRepository.count());
        stats.setUsuariosActivos(contarUsuariosActivos());
        stats.setUsuariosNuevos(contarUsuariosNuevos());

        // Estadísticas de reservas
        stats.setTotalReservas(reservaRepository.count());
        stats.setReservasPendientes(contarReservasPorEstado(EstadoReserva.PENDIENTE));
        stats.setReservasConfirmadas(contarReservasPorEstado(EstadoReserva.CONFIRMADA));
        stats.setReservasCanceladas(contarReservasPorEstado(EstadoReserva.CANCELADA));
        stats.setReservasCompletadas(contarReservasPorEstado(EstadoReserva.COMPLETADA));
        stats.setReservasEnCurso(contarReservasPorEstado(EstadoReserva.EN_CURSO));

        // Estadísticas de recursos
        stats.setTotalCabanas(cabanaRepository.count());
        stats.setCabanasDisponibles(contarCabanasDisponibles());
        stats.setTotalServicios(servicioRepository.count());

        // Estadísticas de ingresos
        stats.setIngresosMes(calcularIngresosMes());
        stats.setIngresosHoy(calcularIngresosHoy());
        stats.setIngresosTotales(calcularIngresosTotales());

        // Datos para gráficos
        stats.setReservasPorDia(obtenerReservasPorDia());
        stats.setIngresosPorMes(obtenerIngresosPorMes());

        // Reservas recientes
        stats.setReservasRecientes(obtenerReservasRecientes());

        return stats;
    }

    private Long contarUsuariosActivos() {
        List<User> usuarios = userRepository.findAll();
        return usuarios.stream()
                .filter(User::isEnabled)
                .count();
    }

    private Long contarUsuariosNuevos() {
        LocalDateTime unMesAtras = LocalDateTime.now().minusMonths(1);
        List<User> usuarios = userRepository.findAll();
        return usuarios.stream()
                .filter(u -> u.getFechaRegistro() != null && u.getFechaRegistro().isAfter(unMesAtras))
                .count();
    }

    private Long contarReservasPorEstado(EstadoReserva estado) {
        return reservaRepository.findByEstado(estado).stream().count();
    }

    private Long contarCabanasDisponibles() {
        // Contar cabañas que no tienen reservas activas para hoy
        LocalDateTime hoy = LocalDateTime.now();
        List<Cabana> cabanas = cabanaRepository.findAll();

        return cabanas.stream()
                .filter(cabana -> {
                    // Verificar si la cabaña está disponible
                    List<Reserva> reservasActivas = reservaRepository.findAll().stream()
                            .filter(r -> r.getRecurso() != null &&
                                       r.getRecurso().getId().equals(cabana.getId()) &&
                                       r.getFechaInicio().isBefore(hoy.plusDays(1)) &&
                                       r.getFechaFin().isAfter(hoy) &&
                                       r.getEstado() != EstadoReserva.CANCELADA)
                            .collect(Collectors.toList());
                    return reservasActivas.isEmpty();
                })
                .count();
    }

    private BigDecimal calcularIngresosMes() {
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finMes = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        List<Pagos> pagos = pagoRepository.findPagosPorPeriodoYEstado(
                "COMPLETADO", inicioMes, finMes);

        return pagos.stream()
                .map(Pagos::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularIngresosHoy() {
        LocalDateTime inicioHoy = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime finHoy = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        List<Pagos> pagos = pagoRepository.findPagosPorPeriodoYEstado(
                "COMPLETADO", inicioHoy, finHoy);

        return pagos.stream()
                .map(Pagos::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularIngresosTotales() {
        List<Pagos> pagos = pagoRepository.findAll();
        return pagos.stream()
                .filter(p -> "COMPLETADO".equals(p.getEstado()))
                .map(Pagos::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ReservasPorDiaDTO> obtenerReservasPorDia() {
        List<ReservasPorDiaDTO> resultado = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Últimos 7 días
        for (int i = 6; i >= 0; i--) {
            LocalDate fecha = hoy.minusDays(i);
            LocalDateTime inicio = fecha.atStartOfDay();
            LocalDateTime fin = fecha.atTime(23, 59, 59);

            long cantidad = reservaRepository.findAll().stream()
                    .filter(r -> r.getFechaReserva() != null &&
                               !r.getFechaReserva().isBefore(inicio) &&
                               !r.getFechaReserva().isAfter(fin))
                    .count();

            resultado.add(ReservasPorDiaDTO.builder()
                    .fecha(fecha.format(formatter))
                    .cantidad(cantidad)
                    .build());
        }

        return resultado;
    }

    private List<IngresosPorMesDTO> obtenerIngresosPorMes() {
        List<IngresosPorMesDTO> resultado = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        // Últimos 6 meses
        for (int i = 5; i >= 0; i--) {
            LocalDate mesActual = hoy.minusMonths(i);
            LocalDateTime inicio = mesActual.withDayOfMonth(1).atStartOfDay();
            LocalDateTime fin = mesActual.withDayOfMonth(mesActual.lengthOfMonth()).atTime(23, 59, 59);

            BigDecimal monto = pagoRepository.findPagosPorPeriodoYEstado("COMPLETADO", inicio, fin)
                    .stream()
                    .map(Pagos::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            resultado.add(IngresosPorMesDTO.builder()
                    .mes(mesActual.format(formatter))
                    .monto(monto)
                    .build());
        }

        return resultado;
    }

    private List<ReservaResumeResponse> obtenerReservasRecientes() {
        List<Reserva> reservas = reservaRepository.findAll();

        return reservas.stream()
                .sorted((a, b) -> {
                    LocalDateTime fechaA = a.getFechaReserva() != null ? a.getFechaReserva() : a.getFechaInicio();
                    LocalDateTime fechaB = b.getFechaReserva() != null ? b.getFechaReserva() : b.getFechaInicio();
                    return fechaB.compareTo(fechaA);
                })
                .limit(10)
                .map(this::mapToReservaResume)
                .collect(Collectors.toList());
    }

    private ReservaResumeResponse mapToReservaResume(Reserva reserva) {
        String nombreUsuario = reserva.getUser() != null
                ? reserva.getUser().getNombres() + " " + reserva.getUser().getApellidos()
                : "N/A";

        String emailUsuario = reserva.getUser() != null ? reserva.getUser().getEmail() : "N/A";

        String nombreRecurso = reserva.getRecurso() != null ? reserva.getRecurso().getNombre() : "N/A";

        LocalDateTime fechaReserva = reserva.getFechaReserva() != null
                ? reserva.getFechaReserva()
                : reserva.getFechaInicio();

        // Determinar tipo de reserva
        String tipoReserva = "SERVICIO"; // Por defecto
        if (reserva.getRecurso() != null) {
            tipoReserva = reserva.getRecurso() instanceof Cabana ? "CABANA_DIA" : "SERVICIO";
        }

        // Obtener información del paquete si existe
        Long paqueteId = null;
        String nombrePaquete = null;
        String estadoPaquete = null;
        BigDecimal precioTotalPaquete = null;
        BigDecimal descuentoPaquete = null;
        BigDecimal precioFinalPaquete = null;
        if (reserva.getPaquete() != null) {
            paqueteId = reserva.getPaquete().getId();
            nombrePaquete = reserva.getPaquete().getNombrePaquete();
            estadoPaquete = reserva.getPaquete().getEstado() != null
                ? reserva.getPaquete().getEstado().name() : null;
            precioTotalPaquete = reserva.getPaquete().getPrecioTotal();
            descuentoPaquete = reserva.getPaquete().getDescuento();
            precioFinalPaquete = reserva.getPaquete().getPrecioFinal();
        }

        return ReservaResumeResponse.builder()
                .id(reserva.getId())
                .nombreUsuario(nombreUsuario)
                .emailUsuario(emailUsuario)
                .nombreRecurso(nombreRecurso)
                .fechaReserva(fechaReserva.toString())
                .estado(reserva.getEstado() != null ? reserva.getEstado().name() : null)
                .precioTotal(reserva.getPrecioTotal())
                // Campos de paquete
                .paqueteId(paqueteId)
                .nombrePaquete(nombrePaquete)
                .estadoPaquete(estadoPaquete)
                .tipoReserva(tipoReserva)
                // Precios del paquete
                .precioTotalPaquete(precioTotalPaquete)
                .descuentoPaquete(descuentoPaquete)
                .precioFinalPaquete(precioFinalPaquete)
                .build();
    }
}
