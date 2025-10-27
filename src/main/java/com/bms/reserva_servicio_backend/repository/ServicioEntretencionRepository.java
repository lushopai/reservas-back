package com.bms.reserva_servicio_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bms.reserva_servicio_backend.enums.EstadoRecurso;
import com.bms.reserva_servicio_backend.models.ServicioEntretencion;

public interface ServicioEntretencionRepository extends JpaRepository<ServicioEntretencion, Long> {

    List<ServicioEntretencion> findByTipoServicio(String tipoServicio);

    List<ServicioEntretencion> findByEstado(EstadoRecurso estado);

    boolean existsByNombre(String nombre);
}