package com.bms.reserva_servicio_backend.service;

import java.time.LocalDate;
import java.util.List;

import com.bms.reserva_servicio_backend.models.Cabana;
import com.bms.reserva_servicio_backend.request.CabanaRequest;
import com.bms.reserva_servicio_backend.response.CabanaResponse;
import com.bms.reserva_servicio_backend.response.SuccessResponse;

public interface CabanaService {

    /**
     * Crear nueva cabaña
     */
    SuccessResponse<CabanaResponse> crearCabana(CabanaRequest request);

    /**
     * Obtener todas las cabañas
     */
    List<CabanaResponse> obtenerTodas();

    /**
     * Obtener cabañas por estado
     */
    List<CabanaResponse> obtenerPorEstado(String estado);

    /**
     * Obtener cabañas disponibles en un rango de fechas
     */
    List<CabanaResponse> obtenerDisponibles(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtener cabaña por ID
     */
    CabanaResponse obtenerPorId(Long id);

    /**
     * Actualizar cabaña
     */
    SuccessResponse<CabanaResponse> actualizarCabana(Long id, CabanaRequest request);

    /**
     * Cambiar estado de cabaña (DISPONIBLE, MANTENIMIENTO, FUERA_SERVICIO)
     */
    SuccessResponse<CabanaResponse> cambiarEstado(Long id, String nuevoEstado);

    /**
     * Eliminar cabaña (soft delete - cambiar a FUERA_SERVICIO)
     */
    SuccessResponse<String> eliminarCabana(Long id);

    /**
     * Obtener entidad Cabana (para uso interno de servicios)
     */
    Cabana obtenerEntidadPorId(Long id);

}
