package com.bms.reserva_servicio_backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bms.reserva_servicio_backend.enums.EstadoReserva;
import com.bms.reserva_servicio_backend.enums.TipoReserva;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "reservas", indexes = {
    @Index(name = "idx_reserva_fechas", columnList = "fechaInicio, fechaFin"),
    @Index(name = "idx_reserva_estado", columnList = "estado"),
    @Index(name = "idx_reserva_recurso", columnList = "recurso_id"),
    @Index(name = "idx_reserva_user", columnList = "user_id"),
    @Index(name = "idx_reserva_paquete", columnList = "paquete_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class Reserva extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Long version;

    @NotNull(message = "El usuario es obligatorio")
    @JsonIgnoreProperties({ "reservas", "paquetesReserva", "password", "roles", "handler", "hibernateLazyInitializer" })
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "El recurso es obligatorio")
    @JsonIgnoreProperties({"inventario", "imagenes", "reservas"})
    @ManyToOne
    @JoinColumn(name = "recurso_id", nullable = false)
    private Recurso recurso;

    @NotNull(message = "La fecha de reserva es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fechaReserva;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @NotNull(message = "El tipo de reserva es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoReserva tipoReserva;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado;

    @NotNull(message = "El precio base es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio base no puede ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @NotNull(message = "El precio de items es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio de items no puede ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioItems;

    @NotNull(message = "El precio total es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio total no puede ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    private List<ItemReservado> itemsReservados = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "paquete_id")
    private PaqueteReserva paquete; // Si es parte de un paquete

    @Column(columnDefinition = "TEXT")
    private String observaciones;

}
