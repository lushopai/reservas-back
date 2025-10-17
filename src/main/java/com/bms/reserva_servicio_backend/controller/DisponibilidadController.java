package com.bms.reserva_servicio_backend.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bms.reserva_servicio_backend.models.BloqueHorario;
import com.bms.reserva_servicio_backend.models.Cabana;
import com.bms.reserva_servicio_backend.models.DisponibilidadCabana;
import com.bms.reserva_servicio_backend.repository.CabanaRepository;
import com.bms.reserva_servicio_backend.repository.DisponibilidadCabanaRepository;
import com.bms.reserva_servicio_backend.request.BloqueoBloqueRequest;
import com.bms.reserva_servicio_backend.request.BloqueoFechasRequest;
import com.bms.reserva_servicio_backend.request.GenerarBloquesRequest;
import com.bms.reserva_servicio_backend.response.BloqueHorarioResponse;
import com.bms.reserva_servicio_backend.response.DisponibilidadCabanaResponse;
import com.bms.reserva_servicio_backend.response.DisponibilidadResponse;
import com.bms.reserva_servicio_backend.response.SuccessResponse;
import com.bms.reserva_servicio_backend.service.DisponibilidadService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/disponibilidad")
@CrossOrigin(origins = "*")
public class DisponibilidadController {

    @Autowired
    private DisponibilidadService disponibilidadService;

    @Autowired
    private DisponibilidadCabanaRepository disponibilidadCabanaRepository;

    @Autowired
    private CabanaRepository cabanaRepository;
    
    /**
     * GET /api/disponibilidad/cabanas/{id}
     * Consultar si una cabaña está disponible
     */
    @GetMapping("/cabanas/{id}")
    public ResponseEntity<DisponibilidadResponse> consultarDisponibilidadCabana(
            @PathVariable Long id,
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {
        
        // El servicio valida:
        // - No hay reservas en conflicto
        // - No hay bloqueos manuales
        // - Estado del recurso
        boolean disponible = disponibilidadService.validarDisponibilidadCabana(
            id, fechaInicio, fechaFin
        );
        
        return ResponseEntity.ok(new DisponibilidadResponse(disponible));
    }
    
    /**
     * GET /api/disponibilidad/servicios/{id}
     * Obtener bloques horarios disponibles
     */
    @GetMapping("/servicios/{id}")
    public ResponseEntity<List<BloqueHorarioResponse>> consultarBloquesDisponibles(
            @PathVariable Long id,
            @RequestParam LocalDate fecha) {

        List<BloqueHorario> bloques = disponibilidadService
            .obtenerBloquesDisponibles(id, fecha);

        List<BloqueHorarioResponse> response = bloques.stream()
            .map(b -> BloqueHorarioResponse.builder()
                .id(b.getId())
                .fecha(b.getFecha())
                .horaInicio(b.getHoraInicio())
                .horaFin(b.getHoraFin())
                .disponible(b.getDisponible())
                .build())
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/disponibilidad/cabanas/{id}/calendario
     * Obtener disponibilidad de cabaña por rango de fechas
     */
    @GetMapping("/cabanas/{id}/calendario")
    public ResponseEntity<List<DisponibilidadCabanaResponse>> obtenerCalendarioCabana(
            @PathVariable Long id,
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {

        Cabana cabana = cabanaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Cabaña no encontrada"));

        List<DisponibilidadCabana> disponibilidades = disponibilidadCabanaRepository
            .findByRangoFechas(id, fechaInicio, fechaFin);

        List<DisponibilidadCabanaResponse> response = disponibilidades.stream()
            .map(d -> DisponibilidadCabanaResponse.builder()
                .id(d.getId())
                .cabanaId(d.getCabana().getId())
                .nombreCabana(cabana.getNombre())
                .fecha(d.getFecha())
                .disponible(d.getDisponible())
                .motivoNoDisponible(d.getMotivoNoDisponible())
                .precioEspecial(d.getPrecioEspecial())
                .build())
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/disponibilidad/cabanas/{id}/fechas-ocupadas
     * Obtener solo las fechas ocupadas/bloqueadas de una cabaña
     */
    @GetMapping("/cabanas/{id}/fechas-ocupadas")
    public ResponseEntity<List<LocalDate>> obtenerFechasOcupadas(
            @PathVariable Long id,
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {

        // Obtener todas las disponibilidades en el rango
        List<DisponibilidadCabana> disponibilidades = disponibilidadCabanaRepository
            .findByRangoFechas(id, fechaInicio, fechaFin);

        // Filtrar solo las fechas no disponibles y extraer las fechas
        List<LocalDate> fechasOcupadas = disponibilidades.stream()
            .filter(d -> !d.getDisponible())
            .map(DisponibilidadCabana::getFecha)
            .collect(Collectors.toList());

        // También agregar fechas de reservas confirmadas
        List<LocalDate> fechasReservadas = disponibilidadService
            .obtenerFechasReservadas(id, fechaInicio, fechaFin);

        // Combinar y eliminar duplicados
        fechasOcupadas.addAll(fechasReservadas);
        List<LocalDate> fechasUnicas = fechasOcupadas.stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        return ResponseEntity.ok(fechasUnicas);
    }

    /**
     * POST /api/disponibilidad/cabanas/{id}/bloquear
     * Bloquear fechas de una cabaña manualmente
     */
    @PostMapping("/cabanas/{id}/bloquear")
    public ResponseEntity<SuccessResponse<String>> bloquearFechasCabana(
            @PathVariable Long id,
            @Valid @RequestBody BloqueoFechasRequest request) {

        Cabana cabana = cabanaRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Cabaña no encontrada"));

        LocalDate fechaActual = request.getFechaInicio();
        while (!fechaActual.isAfter(request.getFechaFin())) {
            final LocalDate fecha = fechaActual;

            DisponibilidadCabana disponibilidad = disponibilidadCabanaRepository
                .findByCabanaIdAndFecha(id, fecha)
                .orElseGet(() -> {
                    DisponibilidadCabana nueva = new DisponibilidadCabana();
                    nueva.setCabana(cabana);
                    nueva.setFecha(fecha);
                    return nueva;
                });

            disponibilidad.setDisponible(false);
            disponibilidad.setMotivoNoDisponible(request.getMotivo() != null ? request.getMotivo() : "BLOQUEO_MANUAL");
            disponibilidad.setPrecioEspecial(request.getPrecioEspecial());
            disponibilidadCabanaRepository.save(disponibilidad);

            fechaActual = fechaActual.plusDays(1);
        }

        return ResponseEntity.ok(SuccessResponse.of(
            "Bloqueadas " + request.getFechaInicio() + " hasta " + request.getFechaFin(),
            "Fechas bloqueadas exitosamente"
        ));
    }

    /**
     * DELETE /api/disponibilidad/cabanas/{id}/desbloquear
     * Desbloquear fechas de una cabaña
     */
    @DeleteMapping("/cabanas/{id}/desbloquear")
    public ResponseEntity<SuccessResponse<String>> desbloquearFechasCabana(
            @PathVariable Long id,
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {

        List<DisponibilidadCabana> disponibilidades = disponibilidadCabanaRepository
            .findByRangoFechas(id, fechaInicio, fechaFin);

        for (DisponibilidadCabana disp : disponibilidades) {
            // Solo desbloquear si es bloqueo manual, no reservas
            if (disp.getMotivoNoDisponible() != null &&
                !disp.getMotivoNoDisponible().equals("RESERVADA")) {
                disp.setDisponible(true);
                disp.setMotivoNoDisponible(null);
                disp.setPrecioEspecial(null);
                disponibilidadCabanaRepository.save(disp);
            }
        }

        return ResponseEntity.ok(SuccessResponse.of(
            "Desbloqueadas " + fechaInicio + " hasta " + fechaFin,
            "Fechas desbloqueadas exitosamente"
        ));
    }

    /**
     * POST /api/disponibilidad/servicios/{id}/bloquear
     * Bloquear bloques horarios de un servicio
     */
    @PostMapping("/servicios/{id}/bloquear")
    public ResponseEntity<SuccessResponse<String>> bloquearBloqueServicio(
            @PathVariable Long id,
            @Valid @RequestBody BloqueoBloqueRequest request) {

        disponibilidadService.bloquearBloqueHorario(
            id,
            request.getFecha(),
            request.getHoraInicio(),
            request.getHoraFin()
        );

        return ResponseEntity.ok(SuccessResponse.of(
            "Bloqueado " + request.getFecha() + " de " + request.getHoraInicio() + " a " + request.getHoraFin(),
            "Bloque horario bloqueado exitosamente"
        ));
    }

    /**
     * POST /api/disponibilidad/servicios/{id}/generar-bloques
     * Generar bloques horarios para un servicio
     */
    @PostMapping("/servicios/{id}/generar-bloques")
    public ResponseEntity<SuccessResponse<String>> generarBloquesHorarios(
            @PathVariable Long id,
            @Valid @RequestBody GenerarBloquesRequest request) {

        disponibilidadService.generarBloquesHorarios(
            id,
            request.getFecha(),
            request.getHoraApertura(),
            request.getHoraCierre(),
            request.getDuracionBloqueMinutos()
        );

        return ResponseEntity.ok(SuccessResponse.of(
            "Generados bloques para " + request.getFecha(),
            "Bloques horarios generados exitosamente"
        ));
    }
}