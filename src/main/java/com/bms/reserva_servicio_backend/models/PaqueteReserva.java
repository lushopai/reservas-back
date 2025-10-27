package com.bms.reserva_servicio_backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bms.reserva_servicio_backend.enums.EstadoPaquete;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "paquetes_reserva", indexes = {
    @Index(name = "idx_paquete_user", columnList = "user_id"),
    @Index(name = "idx_paquete_estado", columnList = "estado"),
    @Index(name = "idx_paquete_fechas", columnList = "fechaInicio, fechaFin")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class PaqueteReserva extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Long version;

    @NotNull(message = "El usuario es obligatorio")
    @JsonIgnoreProperties({ "paquetesReserva", "reservas", "password", "roles", "handler", "hibernateLazyInitializer" })
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "El nombre del paquete es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombrePaquete;

    @NotNull(message = "La fecha de creación es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @OneToMany(mappedBy = "paquete")
    private List<Reserva> reservas = new ArrayList<>();

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPaquete estado;

    @NotNull(message = "El precio total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio total no puede ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;

    @DecimalMin(value = "0.0", inclusive = true, message = "El descuento no puede ser negativo")
    @Column(precision = 10, scale = 2)
    private BigDecimal descuento;

    @NotNull(message = "El precio final es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio final no puede ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioFinal;

    @Column(columnDefinition = "TEXT")
    private String notasEspeciales;

    /**
     * Validación ejecutada antes de persistir o actualizar
     * Valida que fechaFin sea posterior a fechaInicio
     */
    @PrePersist
    @PreUpdate
    private void validarFechas() {
        if (fechaInicio != null && fechaFin != null) {
            if (fechaFin.isBefore(fechaInicio) || fechaFin.isEqual(fechaInicio)) {
                throw new IllegalStateException(
                    "La fecha de fin debe ser posterior a la fecha de inicio"
                );
            }
        }
    }

    /**
     * Verifica si el paquete puede ser modificado
     * Solo se pueden modificar paquetes en estado BORRADOR
     */
    public boolean puedeSerModificado() {
        return estado == EstadoPaquete.BORRADOR;
    }

    /**
     * Verifica si el paquete puede ser cancelado
     */
    public boolean puedeSerCancelado() {
        return estado == EstadoPaquete.BORRADOR || estado == EstadoPaquete.ACTIVO;
    }

}
