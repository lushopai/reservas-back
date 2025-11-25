package com.bms.reserva_servicio_backend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bms.reserva_servicio_backend.enums.EstadoRecurso;
import com.bms.reserva_servicio_backend.enums.EstadoReserva;
import com.bms.reserva_servicio_backend.models.ServicioEntretencion;
import com.bms.reserva_servicio_backend.repository.ServicioEntretencionRepository;
import com.bms.reserva_servicio_backend.request.ServicioRequest;
import com.bms.reserva_servicio_backend.response.ServicioResponse;
import com.bms.reserva_servicio_backend.response.SuccessResponse;
import com.bms.reserva_servicio_backend.service.ServicioService;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class ServicioServiceImpl implements ServicioService {

    @Autowired
    private ServicioEntretencionRepository servicioRepository;

    @Autowired
    private com.bms.reserva_servicio_backend.repository.ReservaRepository reservaRepository;

    @Override
    public SuccessResponse<ServicioResponse> crearServicio(ServicioRequest request) {
        SuccessResponse<ServicioResponse> response = new SuccessResponse<>();

        // Validar que el nombre no esté duplicado
        if (servicioRepository.existsByNombre(request.getNombre())) {
            response.setSuccess(false);
            response.setTimestamp(LocalDateTime.now());
            response.setMessage("Ya existe un servicio con el nombre: " + request.getNombre());
            return response;
        }

        // Crear entidad ServicioEntretencion
        ServicioEntretencion servicio = new ServicioEntretencion();
        mapearRequestAEntidad(request, servicio);

        // Guardar
        ServicioEntretencion servicioGuardado = servicioRepository.save(servicio);

        // Construir respuesta
        ServicioResponse servicioResponse = mapearEntidadAResponse(servicioGuardado);

        response.setSuccess(true);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage("Servicio creado exitosamente");
        response.setData(servicioResponse);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioResponse> obtenerTodos() {
        return servicioRepository.findAll().stream()
                .map(this::mapearEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioResponse> obtenerPorEstado(String estado) {
        EstadoRecurso estadoEnum = EstadoRecurso.valueOf(estado.toUpperCase());
        return servicioRepository.findByEstado(estadoEnum).stream()
                .map(this::mapearEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioResponse> obtenerPorTipo(String tipo) {
        return servicioRepository.findByTipoServicio(tipo).stream()
                .map(this::mapearEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioResponse> obtenerDisponibles(LocalDate fecha) {
        // Obtener servicios con estado DISPONIBLE
        return servicioRepository.findByEstado(EstadoRecurso.DISPONIBLE).stream()
                .map(this::mapearEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioResponse obtenerPorId(Long id) {
        ServicioEntretencion servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con ID: " + id));

        return mapearEntidadAResponse(servicio);
    }

    @Override
    public SuccessResponse<ServicioResponse> actualizarServicio(Long id, ServicioRequest request) {
        SuccessResponse<ServicioResponse> response = new SuccessResponse<>();

        ServicioEntretencion servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con ID: " + id));

        // Validar nombre duplicado si cambió
        if (!servicio.getNombre().equals(request.getNombre())) {
            if (servicioRepository.existsByNombre(request.getNombre())) {
                response.setSuccess(false);
                response.setTimestamp(LocalDateTime.now());
                response.setMessage("Ya existe un servicio con el nombre: " + request.getNombre());
                return response;
            }
        }

        // Actualizar datos
        mapearRequestAEntidad(request, servicio);

        // Guardar
        ServicioEntretencion servicioActualizado = servicioRepository.save(servicio);

        // Construir respuesta
        ServicioResponse servicioResponse = mapearEntidadAResponse(servicioActualizado);

        response.setSuccess(true);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage("Servicio actualizado exitosamente");
        response.setData(servicioResponse);

        return response;
    }

    @Override
    public SuccessResponse<ServicioResponse> cambiarEstado(Long id, String nuevoEstado) {
        SuccessResponse<ServicioResponse> response = new SuccessResponse<>();

        // Validar estado
        if (!esEstadoValido(nuevoEstado)) {
            response.setSuccess(false);
            response.setTimestamp(LocalDateTime.now());
            response.setMessage("Estado inválido. Debe ser: DISPONIBLE, MANTENIMIENTO o FUERA_SERVICIO");
            return response;
        }

        ServicioEntretencion servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con ID: " + id));

        // Si hay reservas activas y se intenta poner en mantenimiento, validar
        EstadoRecurso estadoEnum = EstadoRecurso.valueOf(nuevoEstado.toUpperCase());
        if (estadoEnum == EstadoRecurso.MANTENIMIENTO || estadoEnum == EstadoRecurso.FUERA_SERVICIO) {
            long reservasActivas = reservaRepository.findByRecursoId(id).stream()
                    .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA || r.getEstado() == EstadoReserva.PENDIENTE)
                    .count();

            if (reservasActivas > 0) {
                response.setSuccess(false);
                response.setTimestamp(LocalDateTime.now());
                response.setMessage("No se puede cambiar el estado. El servicio tiene " + reservasActivas + " reserva(s) activa(s)");
                return response;
            }
        }

        servicio.setEstado(estadoEnum);
        ServicioEntretencion servicioActualizado = servicioRepository.save(servicio);

        ServicioResponse servicioResponse = mapearEntidadAResponse(servicioActualizado);

        response.setSuccess(true);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage("Estado cambiado a: " + nuevoEstado);
        response.setData(servicioResponse);

        return response;
    }

    @Override
    public SuccessResponse<String> eliminarServicio(Long id) {
        SuccessResponse<String> response = new SuccessResponse<>();

        ServicioEntretencion servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con ID: " + id));

        // Verificar que no tenga reservas activas
        long reservasActivas = reservaRepository.findByRecursoId(id).stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA || r.getEstado() == EstadoReserva.PENDIENTE)
                .count();

        if (reservasActivas > 0) {
            response.setSuccess(false);
            response.setTimestamp(LocalDateTime.now());
            response.setMessage("No se puede eliminar. El servicio tiene " + reservasActivas + " reserva(s) activa(s)");
            return response;
        }

        // Soft delete: cambiar estado a FUERA_SERVICIO
        servicio.setEstado(EstadoRecurso.FUERA_SERVICIO);
        servicioRepository.save(servicio);

        response.setSuccess(true);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage("Servicio desactivado exitosamente");
        response.setData("Servicio ID: " + id + " ahora está FUERA_SERVICIO");

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioEntretencion obtenerEntidadPorId(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con ID: " + id));
    }

    // Métodos auxiliares privados

    private void mapearRequestAEntidad(ServicioRequest request, ServicioEntretencion servicio) {
        servicio.setNombre(request.getNombre());
        servicio.setDescripcion(request.getDescripcion());
        servicio.setPrecioPorUnidad(request.getPrecioPorUnidad());

        // Convertir String a Enum
        if (request.getEstado() != null) {
            try {
                servicio.setEstado(EstadoRecurso.valueOf(request.getEstado().toUpperCase()));
            } catch (IllegalArgumentException e) {
                servicio.setEstado(EstadoRecurso.DISPONIBLE);
            }
        } else {
            servicio.setEstado(EstadoRecurso.DISPONIBLE);
        }

        servicio.setTipoServicio(request.getTipoServicio());
        servicio.setCapacidadMaxima(request.getCapacidadMaxima());
        servicio.setDuracionBloqueMinutos(request.getDuracionBloqueMinutos());
        servicio.setRequiereSupervision(request.getRequiereSupervision());
        servicio.setHoraApertura(request.getHoraApertura());
        servicio.setHoraCierre(request.getHoraCierre());
    }

    private ServicioResponse mapearEntidadAResponse(ServicioEntretencion servicio) {
        // Obtener el total de reservas desde el repositorio
        int totalReservas = reservaRepository.findByRecursoId(servicio.getId()).size();

        return ServicioResponse.builder()
                .id(servicio.getId())
                .nombre(servicio.getNombre())
                .descripcion(servicio.getDescripcion())
                .precioPorUnidad(servicio.getPrecioPorUnidad())
                .estado(servicio.getEstado() != null ? servicio.getEstado().name() : null)
                .tipoServicio(servicio.getTipoServicio())
                .capacidadMaxima(servicio.getCapacidadMaxima())
                .duracionBloqueMinutos(servicio.getDuracionBloqueMinutos())
                .requiereSupervision(servicio.getRequiereSupervision())
                .horaApertura(servicio.getHoraApertura())
                .horaCierre(servicio.getHoraCierre())
                .totalReservas(totalReservas)
                .disponibleHoy(servicio.getEstado() == EstadoRecurso.DISPONIBLE)
                .bloquesDisponibles(servicio.getBloquesDisponibles() != null ? servicio.getBloquesDisponibles().size() : 0)
                .itemsInventario(servicio.getInventario() != null ? servicio.getInventario().size() : 0)
                .imagenes(servicio.getImagenes())
                .build();
    }

    private boolean esEstadoValido(String estado) {
        try {
            EstadoRecurso.valueOf(estado.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
