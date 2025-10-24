package com.bms.reserva_servicio_backend.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auditoria")
@CrossOrigin(origins = "http://localhost:4200")
public class AuditoriaController {

    @Autowired
    private EntityManager entityManager;

    @GetMapping("/completa")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> ejecutarAuditoriaCompleta() {
        Map<String, Object> resultados = new LinkedHashMap<>();

        try {
            // 1. AUDITORÍA: Items Reservados vs Recurso Correcto
            resultados.put("items_recurso_incorrecto", auditarItemsRecurso());

            // 2. AUDITORÍA: Stock Disponible vs Stock Teórico
            resultados.put("stock_inconsistente", auditarStock());

            // 3. AUDITORÍA: Reservas Finalizadas que No Liberaron Stock
            resultados.put("reservas_finalizadas_sin_liberar", auditarReservasFinalizadas());

            // 4. AUDITORÍA: Paquetes y Estado de sus Reservas
            resultados.put("paquetes_inconsistentes", auditarPaquetes());

            // 5. AUDITORÍA: Distribución de Reservas
            resultados.put("distribucion_reservas", auditarDistribucionReservas());

            // 6. AUDITORÍA: Validación de Fechas
            resultados.put("fechas_incorrectas", auditarFechas());

            // 7. AUDITORÍA: Integridad Referencial
            resultados.put("integridad_referencial", auditarIntegridad());

            // 8. Resumen Ejecutivo
            resultados.put("resumen_ejecutivo", generarResumenEjecutivo());

            return ResponseEntity.ok(resultados);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("stackTrace", Arrays.toString(e.getStackTrace()));
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/corregir-items-incorrectos")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> corregirItemsIncorrectos() {
        Map<String, Object> resultado = new LinkedHashMap<>();

        try {
            // 1. Obtener items incorrectos antes de eliminar
            List<Map<String, Object>> itemsIncorrectos = auditarItemsRecurso();
            resultado.put("items_encontrados", itemsIncorrectos.size());
            resultado.put("items_eliminados", itemsIncorrectos);

            // 2. Eliminar items incorrectos uno por uno
            int eliminados = 0;
            for (Map<String, Object> item : itemsIncorrectos) {
                Object itemReservadoId = item.get("col_0");
                // FIX: Usar parámetros preparados para prevenir SQL injection
                String sqlDelete = "DELETE FROM items_reservados WHERE id = :id";
                Query query = entityManager.createNativeQuery(sqlDelete);
                query.setParameter("id", itemReservadoId);
                int deleted = query.executeUpdate();
                eliminados += deleted;
            }
            resultado.put("registros_eliminados", eliminados);

            // 3. Verificar que no quedan items incorrectos
            List<Map<String, Object>> verificacion = auditarItemsRecurso();
            resultado.put("items_incorrectos_restantes", verificacion.size());
            resultado.put("exito", verificacion.isEmpty());

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("exito", false);
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/recalcular-stock")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> recalcularStock() {
        Map<String, Object> resultado = new LinkedHashMap<>();

        try {
            // 1. Ver estado antes de la corrección
            List<Map<String, Object>> stockAntes = auditarStock();
            resultado.put("items_inconsistentes_antes", stockAntes.size());

            // 2. Recalcular stock de todos los items
            String sql = """
                UPDATE items_inventario ii
                SET cantidad_disponible = cantidad_total - (
                    SELECT COALESCE(SUM(ir.cantidad), 0)
                    FROM items_reservados ir
                    JOIN reservas r ON ir.reserva_id = r.id
                    WHERE ir.item_id = ii.id
                    AND r.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO')
                )
                """;

            int actualizados = entityManager.createNativeQuery(sql).executeUpdate();
            resultado.put("items_actualizados", actualizados);

            // 3. Verificar que todos quedaron correctos
            List<Map<String, Object>> stockDespues = auditarStock();
            resultado.put("items_inconsistentes_despues", stockDespues.size());
            resultado.put("exito", stockDespues.isEmpty());

            // 4. Detalles de items corregidos
            String sqlDetalle = """
                SELECT
                    ii.id,
                    ii.nombre,
                    r.nombre as recurso,
                    ii.cantidad_total,
                    ii.cantidad_disponible,
                    (SELECT COALESCE(SUM(ir.cantidad), 0)
                     FROM items_reservados ir
                     JOIN reservas res ON ir.reserva_id = res.id
                     WHERE ir.item_id = ii.id
                     AND res.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO')) as cantidad_reservada
                FROM items_inventario ii
                JOIN recursos r ON ii.recurso_id = r.id
                ORDER BY ii.id
                """;

            resultado.put("stock_actualizado", ejecutarQuery(sqlDetalle));

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("exito", false);
            return ResponseEntity.internalServerError().body(error);
        }
    }

    private List<Map<String, Object>> auditarItemsRecurso() {
        String sql = """
            SELECT
                ir.id as item_reservado_id,
                ir.reserva_id,
                r.recurso_id as reserva_recurso_id,
                rec_reserva.nombre as recurso_reserva,
                ir.item_id,
                ii.nombre as item_nombre,
                ii.recurso_id as item_recurso_id,
                rec_item.nombre as recurso_item,
                CASE
                    WHEN ii.recurso_id = r.recurso_id THEN 'CORRECTO'
                    ELSE 'INCORRECTO'
                END as validacion,
                ir.cantidad,
                ir.subtotal
            FROM items_reservados ir
            JOIN items_inventario ii ON ir.item_id = ii.id
            JOIN reservas r ON ir.reserva_id = r.id
            JOIN recursos rec_reserva ON r.recurso_id = rec_reserva.id
            JOIN recursos rec_item ON ii.recurso_id = rec_item.id
            WHERE ii.recurso_id != r.recurso_id
            ORDER BY ir.reserva_id
            """;

        return ejecutarQuery(sql);
    }

    private List<Map<String, Object>> auditarStock() {
        String sql = """
            SELECT
                ii.id,
                ii.nombre,
                r.nombre as recurso,
                ii.cantidad_total,
                ii.cantidad_disponible as stock_actual,
                (SELECT COALESCE(SUM(ir.cantidad), 0)
                 FROM items_reservados ir
                 JOIN reservas res ON ir.reserva_id = res.id
                 WHERE ir.item_id = ii.id
                 AND res.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO')) as cantidad_reservada,
                ii.cantidad_total - (SELECT COALESCE(SUM(ir.cantidad), 0)
                                     FROM items_reservados ir
                                     JOIN reservas res ON ir.reserva_id = res.id
                                     WHERE ir.item_id = ii.id
                                     AND res.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO')) as stock_esperado,
                CASE
                    WHEN ii.cantidad_disponible = (ii.cantidad_total - (SELECT COALESCE(SUM(ir.cantidad), 0)
                                                                        FROM items_reservados ir
                                                                        JOIN reservas res ON ir.reserva_id = res.id
                                                                        WHERE ir.item_id = ii.id
                                                                        AND res.estado IN ('PENDIENTE', 'CONFIRMADA', 'EN_CURSO')))
                    THEN 'CORRECTO'
                    ELSE 'INCONSISTENTE'
                END as validacion_stock
            FROM items_inventario ii
            JOIN recursos r ON ii.recurso_id = r.id
            HAVING validacion_stock = 'INCONSISTENTE'
            ORDER BY ii.id
            """;

        return ejecutarQuery(sql);
    }

    private List<Map<String, Object>> auditarReservasFinalizadas() {
        String sql = """
            SELECT
                r.id as reserva_id,
                r.estado,
                r.fecha_fin,
                DATEDIFF(NOW(), r.fecha_fin) as dias_desde_fin,
                ir.item_id,
                ii.nombre as item_nombre,
                ir.cantidad as cantidad_reservada,
                ii.cantidad_disponible as stock_actual
            FROM reservas r
            JOIN items_reservados ir ON r.id = ir.reserva_id
            JOIN items_inventario ii ON ir.item_id = ii.id
            WHERE r.estado IN ('COMPLETADA', 'CANCELADA')
            ORDER BY r.fecha_fin DESC
            """;

        return ejecutarQuery(sql);
    }

    private List<Map<String, Object>> auditarPaquetes() {
        String sql = """
            SELECT
                pr.id as paquete_id,
                pr.nombre_paquete,
                pr.estado as estado_paquete,
                pr.fecha_creacion,
                pr.precio_total,
                pr.descuento,
                pr.precio_final,
                COUNT(r.id) as cantidad_reservas,
                SUM(r.precio_total) as suma_precios_reservas,
                CASE
                    WHEN pr.precio_total = SUM(r.precio_total) THEN 'CORRECTO'
                    ELSE 'PRECIO_NO_COINCIDE'
                END as validacion_precio,
                CASE
                    WHEN pr.descuento = 0 THEN 'SIN_DESCUENTO'
                    ELSE 'TIENE_DESCUENTO'
                END as validacion_descuento,
                CASE
                    WHEN pr.fecha_creacion IS NOT NULL THEN 'TIENE_FECHA'
                    ELSE 'FECHA_NULL'
                END as validacion_fecha
            FROM paquetes_reserva pr
            LEFT JOIN reservas r ON r.paquete_id = pr.id
            GROUP BY pr.id, pr.nombre_paquete, pr.estado, pr.fecha_creacion, pr.precio_total, pr.descuento, pr.precio_final
            HAVING validacion_precio != 'CORRECTO' OR validacion_descuento != 'SIN_DESCUENTO' OR validacion_fecha != 'TIENE_FECHA'
            ORDER BY pr.id
            """;

        return ejecutarQuery(sql);
    }

    private List<Map<String, Object>> auditarDistribucionReservas() {
        String sql = """
            SELECT
                CASE
                    WHEN paquete_id IS NULL THEN 'Reservas Individuales'
                    ELSE 'Reservas de Paquete'
                END as tipo,
                COUNT(*) as cantidad,
                COUNT(DISTINCT user_id) as clientes_unicos,
                estado,
                SUM(precio_total) as total_ventas
            FROM reservas
            GROUP BY tipo, estado
            ORDER BY tipo, estado
            """;

        return ejecutarQuery(sql);
    }

    private List<Map<String, Object>> auditarFechas() {
        String sql = """
            SELECT
                id,
                estado,
                fecha_inicio,
                fecha_fin,
                CASE
                    WHEN fecha_inicio >= NOW() THEN 'FUTURA'
                    WHEN fecha_fin < NOW() AND estado NOT IN ('COMPLETADA', 'CANCELADA') THEN 'DEBERIA_ESTAR_COMPLETADA'
                    WHEN fecha_fin >= NOW() AND fecha_inicio <= NOW() THEN 'EN_CURSO'
                    ELSE 'OK'
                END as validacion_fechas,
                CASE
                    WHEN fecha_fin > fecha_inicio THEN 'CORRECTO'
                    ELSE 'FIN_ANTES_DE_INICIO'
                END as validacion_logica
            FROM reservas
            WHERE estado NOT IN ('CANCELADA')
            HAVING validacion_fechas = 'DEBERIA_ESTAR_COMPLETADA' OR validacion_logica != 'CORRECTO'
            ORDER BY validacion_fechas DESC, fecha_inicio
            """;

        return ejecutarQuery(sql);
    }

    private List<Map<String, Object>> auditarIntegridad() {
        List<Map<String, Object>> resultados = new ArrayList<>();

        String[] queries = {
            "SELECT 'Reservas sin usuario' as problema, COUNT(*) as cantidad FROM reservas WHERE user_id IS NULL",
            "SELECT 'Reservas sin recurso' as problema, COUNT(*) as cantidad FROM reservas WHERE recurso_id IS NULL",
            "SELECT 'Paquetes sin usuario' as problema, COUNT(*) as cantidad FROM paquetes_reserva WHERE user_id IS NULL",
            "SELECT 'Items sin recurso' as problema, COUNT(*) as cantidad FROM items_inventario WHERE recurso_id IS NULL",
            "SELECT 'Items reservados sin reserva' as problema, COUNT(*) as cantidad FROM items_reservados ir WHERE NOT EXISTS (SELECT 1 FROM reservas r WHERE r.id = ir.reserva_id)",
            "SELECT 'Items reservados sin item' as problema, COUNT(*) as cantidad FROM items_reservados ir WHERE NOT EXISTS (SELECT 1 FROM items_inventario ii WHERE ii.id = ir.item_id)"
        };

        for (String sql : queries) {
            resultados.addAll(ejecutarQuery(sql));
        }

        return resultados;
    }

    private List<Map<String, Object>> generarResumenEjecutivo() {
        String sql = """
            SELECT 'Total Paquetes' as metrica, COUNT(*) as valor FROM paquetes_reserva
            UNION ALL
            SELECT 'Total Reservas', COUNT(*) FROM reservas
            UNION ALL
            SELECT 'Total Items Inventario', COUNT(*) FROM items_inventario
            UNION ALL
            SELECT 'Total Items Reservados', COUNT(*) FROM items_reservados
            UNION ALL
            SELECT 'Reservas Activas', COUNT(*) FROM reservas WHERE estado IN ('PENDIENTE', 'CONFIRMADA')
            UNION ALL
            SELECT 'Paquetes Pagados', COUNT(*) FROM paquetes_reserva WHERE estado = 'CONFIRMADA'
            UNION ALL
            SELECT 'Items con Stock Bajo', COUNT(*) FROM items_inventario WHERE cantidad_disponible < 3
            """;

        return ejecutarQuery(sql);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> ejecutarQuery(String sql) {
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        if (results.isEmpty()) {
            return new ArrayList<>();
        }

        // Obtener nombres de columnas (aproximación básica)
        List<Map<String, Object>> mappedResults = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> map = new LinkedHashMap<>();

            // Si es un solo valor, usamos un nombre genérico
            if (row.length == 1) {
                map.put("value", row[0]);
            } else {
                // Para múltiples columnas, usamos índices
                for (int i = 0; i < row.length; i++) {
                    map.put("col_" + i, row[i]);
                }
            }

            mappedResults.add(map);
        }

        return mappedResults;
    }
}
