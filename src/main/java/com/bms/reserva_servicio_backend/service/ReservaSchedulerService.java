package com.bms.reserva_servicio_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bms.reserva_servicio_backend.models.Reserva;
import com.bms.reserva_servicio_backend.repository.ReservaRepository;

/**
 * Servicio para tareas programadas relacionadas con reservas
 * - Liberar stock de reservas completadas
 * - Actualizar estados de reservas según fechas
 */
@Service
@Transactional
public class ReservaSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(ReservaSchedulerService.class);

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private InventarioService inventarioService;

    /**
     * Job que se ejecuta cada hora para:
     * 1. Completar automáticamente reservas que ya finalizaron
     * 2. Liberar el stock de items reservados
     * 3. Liberar la disponibilidad de los recursos
     */
    @Scheduled(fixedDelay = 3600000, initialDelay = 60000) // Cada hora, delay inicial de 1 minuto
    public void procesarReservasFinalizadas() {
        logger.info("Iniciando procesamiento de reservas finalizadas...");

        try {
            LocalDateTime ahora = LocalDateTime.now();

            // Buscar reservas EN_CURSO o CONFIRMADA que ya finalizaron
            List<Reserva> reservasFinalizadas = reservaRepository
                .findByEstadoInAndFechaFinBefore(
                    List.of("EN_CURSO", "CONFIRMADA"),
                    ahora
                );

            if (reservasFinalizadas.isEmpty()) {
                logger.info("No hay reservas para completar");
                return;
            }

            logger.info("Encontradas {} reservas para completar", reservasFinalizadas.size());

            int completadas = 0;
            int errores = 0;

            for (Reserva reserva : reservasFinalizadas) {
                try {
                    // Cambiar estado a COMPLETADA
                    String estadoAnterior = reserva.getEstado();
                    reserva.setEstado("COMPLETADA");
                    reservaRepository.save(reserva);

                    // Liberar items del inventario
                    inventarioService.liberarItems(reserva);

                    // Nota: La disponibilidad ya se maneja en el DisponibilidadService
                    // y se libera automáticamente cuando pasa la fecha

                    logger.info("Reserva #{} completada y stock liberado (estado anterior: {})",
                               reserva.getId(), estadoAnterior);
                    completadas++;

                } catch (Exception e) {
                    errores++;
                    logger.error("Error al procesar reserva #{}: {}",
                               reserva.getId(), e.getMessage(), e);
                }
            }

            logger.info("Procesamiento completado: {} reservas completadas, {} errores",
                       completadas, errores);

        } catch (Exception e) {
            logger.error("Error general en procesarReservasFinalizadas: {}", e.getMessage(), e);
        }
    }

    /**
     * Job que se ejecuta cada 30 minutos para actualizar reservas a EN_CURSO
     * cuando la fecha de inicio ya pasó
     */
    @Scheduled(fixedDelay = 1800000, initialDelay = 30000) // Cada 30 minutos, delay inicial de 30 segundos
    public void actualizarReservasEnCurso() {
        logger.info("🔄 Actualizando reservas a EN_CURSO...");

        try {
            LocalDateTime ahora = LocalDateTime.now();

            // Buscar reservas CONFIRMADA cuya fecha de inicio ya pasó pero aún no terminaron
            List<Reserva> reservasParaIniciar = reservaRepository
                .findByEstadoAndFechaInicioBefore("CONFIRMADA", ahora);

            // Filtrar solo las que no han terminado
            List<Reserva> reservasEnCurso = reservasParaIniciar.stream()
                .filter(r -> r.getFechaFin().isAfter(ahora))
                .toList();

            if (reservasEnCurso.isEmpty()) {
                logger.info("✅ No hay reservas para iniciar");
                return;
            }

            logger.info("📋 Encontradas {} reservas para marcar como EN_CURSO", reservasEnCurso.size());

            int actualizadas = 0;
            for (Reserva reserva : reservasEnCurso) {
                try {
                    reserva.setEstado("EN_CURSO");
                    reservaRepository.save(reserva);
                    logger.info("✅ Reserva #{} actualizada a EN_CURSO", reserva.getId());
                    actualizadas++;
                } catch (Exception e) {
                    logger.error("❌ Error al actualizar reserva #{}: {}", reserva.getId(), e.getMessage());
                }
            }

            logger.info("✅ Actualizadas {} reservas a EN_CURSO", actualizadas);

        } catch (Exception e) {
            logger.error("❌ Error en actualizarReservasEnCurso: {}", e.getMessage(), e);
        }
    }

    /**
     * Job manual para recalcular stock de todos los items (útil para correcciones)
     * No se ejecuta automáticamente, solo llamando directamente al método
     */
    public void recalcularTodoElStock() {
        logger.info("🔄 Recalculando stock de todos los items...");
        // Este método ya existe en AuditoriaController
        // Se podría mover aquí si se necesita ejecutar programadamente
        logger.info("✅ Para recalcular stock, usar: POST /api/auditoria/recalcular-stock");
    }
}
