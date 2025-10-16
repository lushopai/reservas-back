package com.bms.reserva_servicio_backend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bms.reserva_servicio_backend.models.Cabana;
import com.bms.reserva_servicio_backend.repository.CabanaRepository;
import com.bms.reserva_servicio_backend.request.CabanaRequest;
import com.bms.reserva_servicio_backend.response.CabanaResponse;
import com.bms.reserva_servicio_backend.response.SuccessResponse;
import com.bms.reserva_servicio_backend.service.CabanaService;
import com.bms.reserva_servicio_backend.service.DisponibilidadService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class CabanaServiceImpl implements CabanaService {

    @Autowired
    private CabanaRepository cabanaRepository;

    @Autowired
    private DisponibilidadService disponibilidadService;

    @Override
    public SuccessResponse<CabanaResponse> crearCabana(CabanaRequest request) {
        SuccessResponse<CabanaResponse> response = new SuccessResponse<>();

        // Validar que el nombre no esté duplicado
        if (cabanaRepository.existsByNombre(request.getNombre())) {
            response.setSuccess(false);
            response.setTimestamp(LocalDateTime.now());
            response.setMessage("Ya existe una cabaña con el nombre: " + request.getNombre());
            return response;
        }

        // Crear entidad Cabana
        Cabana cabana = new Cabana();
        mapearRequestAEntidad(request, cabana);

        // Guardar
        Cabana cabanaGuardada = cabanaRepository.save(cabana);

        // Construir respuesta
        CabanaResponse cabanaResponse = mapearEntidadAResponse(cabanaGuardada);

        response.setSuccess(true);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage("Cabaña creada exitosamente");
        response.setData(cabanaResponse);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CabanaResponse> obtenerTodas() {
        return cabanaRepository.findAll().stream()
                .map(this::mapearEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CabanaResponse> obtenerPorEstado(String estado) {
        return cabanaRepository.findByEstado(estado).stream()
                .map(this::mapearEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CabanaResponse> obtenerDisponibles(LocalDate fechaInicio, LocalDate fechaFin) {
        // Obtener cabañas con estado DISPONIBLE
        List<Cabana> cabanasDisponibles = cabanaRepository.findByEstado("DISPONIBLE");

        // Filtrar por disponibilidad en las fechas
        return cabanasDisponibles.stream()
                .filter(cabana -> disponibilidadService.validarDisponibilidadCabana(
                        cabana.getId(), fechaInicio, fechaFin))
                .map(this::mapearEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CabanaResponse obtenerPorId(Long id) {
        Cabana cabana = cabanaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cabaña no encontrada con ID: " + id));

        return mapearEntidadAResponse(cabana);
    }

    @Override
    public SuccessResponse<CabanaResponse> actualizarCabana(Long id, CabanaRequest request) {
        SuccessResponse<CabanaResponse> response = new SuccessResponse<>();

        Cabana cabana = cabanaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cabaña no encontrada con ID: " + id));

        // Validar nombre duplicado si cambió
        if (!cabana.getNombre().equals(request.getNombre())) {
            if (cabanaRepository.existsByNombre(request.getNombre())) {
                response.setSuccess(false);
                response.setTimestamp(LocalDateTime.now());
                response.setMessage("Ya existe una cabaña con el nombre: " + request.getNombre());
                return response;
            }
        }

        // Actualizar datos
        mapearRequestAEntidad(request, cabana);

        // Guardar
        Cabana cabanaActualizada = cabanaRepository.save(cabana);

        // Construir respuesta
        CabanaResponse cabanaResponse = mapearEntidadAResponse(cabanaActualizada);

        response.setSuccess(true);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage("Cabaña actualizada exitosamente");
        response.setData(cabanaResponse);

        return response;
    }

    @Override
    public SuccessResponse<CabanaResponse> cambiarEstado(Long id, String nuevoEstado) {
        SuccessResponse<CabanaResponse> response = new SuccessResponse<>();

        // Validar estado
        if (!esEstadoValido(nuevoEstado)) {
            response.setSuccess(false);
            response.setTimestamp(LocalDateTime.now());
            response.setMessage("Estado inválido. Debe ser: DISPONIBLE, MANTENIMIENTO o FUERA_SERVICIO");
            return response;
        }

        Cabana cabana = cabanaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cabaña no encontrada con ID: " + id));

        // Si hay reservas activas y se intenta poner en mantenimiento, validar
        if ("MANTENIMIENTO".equals(nuevoEstado) || "FUERA_SERVICIO".equals(nuevoEstado)) {
            long reservasActivas = cabana.getReservas().stream()
                    .filter(r -> "CONFIRMADA".equals(r.getEstado()) || "PENDIENTE".equals(r.getEstado()))
                    .count();

            if (reservasActivas > 0) {
                response.setSuccess(false);
                response.setTimestamp(LocalDateTime.now());
                response.setMessage("No se puede cambiar el estado. La cabaña tiene " + reservasActivas + " reserva(s) activa(s)");
                return response;
            }
        }

        cabana.setEstado(nuevoEstado);
        Cabana cabanaActualizada = cabanaRepository.save(cabana);

        CabanaResponse cabanaResponse = mapearEntidadAResponse(cabanaActualizada);

        response.setSuccess(true);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage("Estado cambiado a: " + nuevoEstado);
        response.setData(cabanaResponse);

        return response;
    }

    @Override
    public SuccessResponse<String> eliminarCabana(Long id) {
        SuccessResponse<String> response = new SuccessResponse<>();

        Cabana cabana = cabanaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cabaña no encontrada con ID: " + id));

        // Verificar que no tenga reservas activas
        long reservasActivas = cabana.getReservas().stream()
                .filter(r -> "CONFIRMADA".equals(r.getEstado()) || "PENDIENTE".equals(r.getEstado()))
                .count();

        if (reservasActivas > 0) {
            response.setSuccess(false);
            response.setTimestamp(LocalDateTime.now());
            response.setMessage("No se puede eliminar. La cabaña tiene " + reservasActivas + " reserva(s) activa(s)");
            return response;
        }

        // Soft delete: cambiar estado a FUERA_SERVICIO
        cabana.setEstado("FUERA_SERVICIO");
        cabanaRepository.save(cabana);

        response.setSuccess(true);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage("Cabaña desactivada exitosamente");
        response.setData("Cabaña ID: " + id + " ahora está FUERA_SERVICIO");

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Cabana obtenerEntidadPorId(Long id) {
        return cabanaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cabaña no encontrada con ID: " + id));
    }

    // Métodos auxiliares privados

    private void mapearRequestAEntidad(CabanaRequest request, Cabana cabana) {
        cabana.setNombre(request.getNombre());
        cabana.setDescripcion(request.getDescripcion());
        cabana.setPrecioPorUnidad(request.getPrecioPorUnidad());
        cabana.setEstado(request.getEstado());
        cabana.setCapacidadPersonas(request.getCapacidadPersonas());
        cabana.setNumeroHabitaciones(request.getNumeroHabitaciones());
        cabana.setNumeroBanos(request.getNumeroBanos());
        cabana.setMetrosCuadrados(request.getMetrosCuadrados());
        cabana.setTipoCabana(request.getTipoCabana());
        cabana.setServiciosIncluidos(request.getServiciosIncluidos());
    }

    private CabanaResponse mapearEntidadAResponse(Cabana cabana) {
        return CabanaResponse.builder()
                .id(cabana.getId())
                .nombre(cabana.getNombre())
                .descripcion(cabana.getDescripcion())
                .precioPorUnidad(cabana.getPrecioPorUnidad())
                .estado(cabana.getEstado())
                .capacidadPersonas(cabana.getCapacidadPersonas())
                .numeroHabitaciones(cabana.getNumeroHabitaciones())
                .numeroBanos(cabana.getNumeroBanos())
                .metrosCuadrados(cabana.getMetrosCuadrados())
                .tipoCabana(cabana.getTipoCabana())
                .serviciosIncluidos(cabana.getServiciosIncluidos())
                .totalReservas(cabana.getReservas() != null ? cabana.getReservas().size() : 0)
                .disponibleHoy("DISPONIBLE".equals(cabana.getEstado()))
                .itemsInventario(cabana.getInventario() != null ? cabana.getInventario().size() : 0)
                .imagenes(cabana.getImagenes())
                .build();
    }

    private boolean esEstadoValido(String estado) {
        return "DISPONIBLE".equals(estado) ||
               "MANTENIMIENTO".equals(estado) ||
               "FUERA_SERVICIO".equals(estado);
    }

}
