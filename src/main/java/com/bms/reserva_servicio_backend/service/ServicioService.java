package com.bms.reserva_servicio_backend.service;

import java.time.LocalDate;
import java.util.List;

import com.bms.reserva_servicio_backend.models.ServicioEntretencion;
import com.bms.reserva_servicio_backend.request.ServicioRequest;
import com.bms.reserva_servicio_backend.response.ServicioResponse;
import com.bms.reserva_servicio_backend.response.SuccessResponse;

public interface ServicioService {

    /**
     * Crear nuevo servicio de entretención
     */
    SuccessResponse<ServicioResponse> crearServicio(ServicioRequest request);

    /**
     * Obtener todos los servicios
     */
    List<ServicioResponse> obtenerTodos();

    /**
     * Obtener servicios por estado
     */
    List<ServicioResponse> obtenerPorEstado(String estado);

    /**
     * Obtener servicios por tipo
     */
    List<ServicioResponse> obtenerPorTipo(String tipo);

    /**
     * Obtener servicios disponibles en una fecha específica
     */
    List<ServicioResponse> obtenerDisponibles(LocalDate fecha);

    /**
     * Obtener servicio por ID
     */
    ServicioResponse obtenerPorId(Long id);

    /**
     * Actualizar servicio
     */
    SuccessResponse<ServicioResponse> actualizarServicio(Long id, ServicioRequest request);

    /**
     * Cambiar estado de servicio (DISPONIBLE, MANTENIMIENTO, FUERA_SERVICIO)
     */
    SuccessResponse<ServicioResponse> cambiarEstado(Long id, String nuevoEstado);

    /**
     * Eliminar servicio (soft delete - cambiar a FUERA_SERVICIO)
     */
    SuccessResponse<String> eliminarServicio(Long id);

    /**
     * Obtener entidad ServicioEntretencion (para uso interno de servicios)
     */
    ServicioEntretencion obtenerEntidadPorId(Long id);

}
