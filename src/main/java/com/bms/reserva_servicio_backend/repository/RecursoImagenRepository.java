package com.bms.reserva_servicio_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bms.reserva_servicio_backend.models.RecursoImagen;

@Repository
public interface RecursoImagenRepository extends JpaRepository<RecursoImagen, Long> {

    List<RecursoImagen> findByRecursoIdOrderByOrdenVisualizacionAsc(Long recursoId);

    RecursoImagen findByRecursoIdAndEsPrincipalTrue(Long recursoId);

    void deleteByRecursoId(Long recursoId);
}
