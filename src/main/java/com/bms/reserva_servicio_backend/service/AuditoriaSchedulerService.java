package com.bms.reserva_servicio_backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Servicio para ejecutar auditorías programadas del sistema
 * - Verifica integridad de datos
 * - Detecta problemas de stock
 * - Identifica items en recursos incorrectos
 */
@Service
public class AuditoriaSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(AuditoriaSchedulerService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Auditoría completa que se ejecuta diariamente a las 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void auditoriaDiaria() {
        logger.info("🔍 ==========================================");
        logger.info("🔍 INICIANDO AUDITORÍA DIARIA DEL SISTEMA");
        logger.info("🔍 Fecha: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        logger.info("🔍 ==========================================");

        try {
            // 1. Auditar items en recursos incorrectos
            auditarItemsIncorrectos();

            // 2. Auditar stock inconsistente
            auditarStockInconsistente();

            // 3. Auditar integridad referencial
            auditarIntegridadReferencial();

            // 4. Auditar reservas con fechas incorrectas
            auditarFechasIncorrectas();

            logger.info("✅ AUDITORÍA DIARIA COMPLETADA EXITOSAMENTE");

        } catch (Exception e) {
            logger.error("❌ ERROR EN AUDITORÍA DIARIA: {}", e.getMessage(), e);
        }

        logger.info("🔍 ==========================================\n");
    }

    /**
     * Auditoría rápida cada 6 horas para detectar problemas críticos
     */
    @Scheduled(fixedDelay = 21600000, initialDelay = 300000) // Cada 6 horas, delay inicial 5 minutos
    public void auditoriaRapida() {
        logger.info("🔍 Ejecutando auditoría rápida...");

        try {
            // Solo verificar problemas críticos
            int itemsIncorrectos = contarItemsIncorrectos();
            int stockInconsistente = contarStockInconsistente();

            if (itemsIncorrectos > 0 || stockInconsistente > 0) {
                logger.warn("⚠️ PROBLEMAS DETECTADOS:");
                if (itemsIncorrectos > 0) {
                    logger.warn("  - {} items en recursos incorrectos", itemsIncorrectos);
                }
                if (stockInconsistente > 0) {
                    logger.warn("  - {} items con stock inconsistente", stockInconsistente);
                }
                logger.warn("  Ejecutar: POST /api/auditoria/corregir-items-incorrectos");
                logger.warn("  Ejecutar: POST /api/auditoria/recalcular-stock");
            } else {
                logger.info("✅ Sistema saludable - No se detectaron problemas");
            }

        } catch (Exception e) {
            logger.error("❌ Error en auditoría rápida: {}", e.getMessage());
        }
    }

    private void auditarItemsIncorrectos() {
        logger.info("📋 Auditando items en recursos incorrectos...");

        String sql = """
            SELECT COUNT(*) FROM items_reservados ir
            JOIN items_inventario ii ON ir.item_id = ii.id
            JOIN reservas r ON ir.reserva_id = r.id
            WHERE ii.recurso_id != r.recurso_id
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);

        if (count != null && count > 0) {
            logger.error("❌ CRÍTICO: {} items asociados a recursos incorrectos", count);

            // Obtener detalles
            String sqlDetalle = """
                SELECT
                    ir.id as item_reservado_id,
                    ir.reserva_id,
                    ii.nombre as item_nombre,
                    r_recurso.nombre as recurso_reserva,
                    i_recurso.nombre as recurso_item
                FROM items_reservados ir
                JOIN items_inventario ii ON ir.item_id = ii.id
                JOIN reservas r ON ir.reserva_id = r.id
                JOIN recursos r_recurso ON r.recurso_id = r_recurso.id
                JOIN recursos i_recurso ON ii.recurso_id = i_recurso.id
                WHERE ii.recurso_id != r.recurso_id
                LIMIT 5
                """;

            List<Map<String, Object>> detalles = jdbcTemplate.queryForList(sqlDetalle);
            for (Map<String, Object> detalle : detalles) {
                logger.error("  Item '{}' de '{}' en reserva de '{}'",
                           detalle.get("item_nombre"),
                           detalle.get("recurso_item"),
                           detalle.get("recurso_reserva"));
            }
        } else {
            logger.info("✅ No hay items en recursos incorrectos");
        }
    }

    private void auditarStockInconsistente() {
        logger.info("📋 Auditando consistencia de stock...");

        String sql = """
            SELECT COUNT(*) FROM items_inventario ii
            WHERE ii.cantidad_disponible != (
                ii.cantidad_total - (
                    SELECT COALESCE(SUM(ir.cantidad), 0)
                    FROM items_reservados ir
                    JOIN reservas r ON ir.reserva_id = r.id
                    WHERE ir.item_id = ii.id
                    AND r.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO')
                )
            )
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);

        if (count != null && count > 0) {
            logger.error("❌ CRÍTICO: {} items con stock inconsistente", count);

            // Obtener detalles
            String sqlDetalle = """
                SELECT
                    ii.id,
                    ii.nombre,
                    ii.cantidad_disponible as stock_registrado,
                    ii.cantidad_total - COALESCE((
                        SELECT SUM(ir.cantidad)
                        FROM items_reservados ir
                        JOIN reservas r ON ir.reserva_id = r.id
                        WHERE ir.item_id = ii.id
                        AND r.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO')
                    ), 0) as stock_esperado
                FROM items_inventario ii
                WHERE ii.cantidad_disponible != (
                    ii.cantidad_total - COALESCE((
                        SELECT SUM(ir.cantidad)
                        FROM items_reservados ir
                        JOIN reservas r ON ir.reserva_id = r.id
                        WHERE ir.item_id = ii.id
                        AND r.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO')
                    ), 0)
                )
                LIMIT 5
                """;

            List<Map<String, Object>> detalles = jdbcTemplate.queryForList(sqlDetalle);
            for (Map<String, Object> detalle : detalles) {
                logger.error("  Item '{}': Stock registrado = {}, esperado = {}",
                           detalle.get("nombre"),
                           detalle.get("stock_registrado"),
                           detalle.get("stock_esperado"));
            }
        } else {
            logger.info("✅ Stock de todos los items es consistente");
        }
    }

    private void auditarIntegridadReferencial() {
        logger.info("📋 Auditando integridad referencial...");

        boolean hayProblemas = false;

        // Reservas sin usuario
        Integer reservasSinUsuario = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM reservas WHERE user_id IS NULL",
            Integer.class
        );
        if (reservasSinUsuario != null && reservasSinUsuario > 0) {
            logger.error("❌ {} reservas sin usuario", reservasSinUsuario);
            hayProblemas = true;
        }

        // Items reservados huérfanos
        Integer itemsHuerfanos = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM items_reservados ir WHERE NOT EXISTS (SELECT 1 FROM reservas r WHERE r.id = ir.reserva_id)",
            Integer.class
        );
        if (itemsHuerfanos != null && itemsHuerfanos > 0) {
            logger.error("❌ {} items reservados huérfanos", itemsHuerfanos);
            hayProblemas = true;
        }

        if (!hayProblemas) {
            logger.info("✅ Integridad referencial correcta");
        }
    }

    private void auditarFechasIncorrectas() {
        logger.info("📋 Auditando fechas de reservas...");

        String sql = """
            SELECT COUNT(*) FROM reservas
            WHERE estado NOT IN ('CANCELADA')
            AND (
                fecha_fin < fecha_inicio
                OR (fecha_fin < NOW() AND estado NOT IN ('COMPLETADA', 'CANCELADA'))
            )
            """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);

        if (count != null && count > 0) {
            logger.error("❌ {} reservas con fechas incorrectas", count);
        } else {
            logger.info("✅ Todas las fechas son correctas");
        }
    }

    private int contarItemsIncorrectos() {
        String sql = """
            SELECT COUNT(*) FROM items_reservados ir
            JOIN items_inventario ii ON ir.item_id = ii.id
            JOIN reservas r ON ir.reserva_id = r.id
            WHERE ii.recurso_id != r.recurso_id
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    private int contarStockInconsistente() {
        String sql = """
            SELECT COUNT(*) FROM items_inventario ii
            WHERE ii.cantidad_disponible != (
                ii.cantidad_total - (
                    SELECT COALESCE(SUM(ir.cantidad), 0)
                    FROM items_reservados ir
                    JOIN reservas r ON ir.reserva_id = r.id
                    WHERE ir.item_id = ii.id
                    AND r.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO')
                )
            )
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
