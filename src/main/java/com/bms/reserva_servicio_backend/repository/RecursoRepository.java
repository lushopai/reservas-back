package com.bms.reserva_servicio_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bms.reserva_servicio_backend.models.Recurso;

public interface RecursoRepository extends JpaRepository<Recurso, Long> {
    List<Recurso> findByEstado(String estado);
}