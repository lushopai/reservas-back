package com.bms.reserva_servicio_backend.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad para registrar todos los movimientos de inventario
 * Mantiene un historial de entradas, salidas y devoluciones de items
 */
@Entity
@Table(name = "movimientos_inventario")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemsInventario item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento;

    // Referencia a la reserva asociada (si aplica)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    // Usuario que realizó el movimiento
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User usuario;

    @Column(length = 500)
    private String observaciones;

    // Stock antes y después del movimiento (para auditoría)
    @Column(name = "stock_anterior")
    private Integer stockAnterior;

    @Column(name = "stock_posterior")
    private Integer stockPosterior;

    @PrePersist
    protected void onCreate() {
        if (fechaMovimiento == null) {
            fechaMovimiento = LocalDateTime.now();
        }
    }

    /**
     * Tipos de movimiento posibles
     */
    public enum TipoMovimiento {
        ENTRADA,        // Entrada de stock (compra, donación, etc.)
        SALIDA,         // Salida por reserva
        DEVOLUCION,     // Devolución de una reserva completada/cancelada
        AJUSTE_POSITIVO,// Ajuste manual positivo
        AJUSTE_NEGATIVO,// Ajuste manual negativo
        PERDIDA,        // Pérdida o robo
        DANO            // Daño del item
    }
}
