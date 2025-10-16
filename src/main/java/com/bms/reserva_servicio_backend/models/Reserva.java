package com.bms.reserva_servicio_backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "reservas")
@Data
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cambiar de "cliente" a "user"
    @JsonIgnoreProperties({ "reservas", "paquetesReserva", "password", "roles", "handler", "hibernateLazyInitializer" })
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnoreProperties({"inventario", "imagenes", "reservas"})
    @ManyToOne
    @JoinColumn(name = "recurso_id")
    private Recurso recurso;

    private LocalDateTime fechaReserva;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String tipoReserva;
    private String estado;

    private BigDecimal precioBase;
    private BigDecimal precioItems;
    private BigDecimal precioTotal;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL)
    private List<ItemReservado> itemsReservados = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "paquete_id")
    private PaqueteReserva paquete; // Si es parte de un paquete

    @Column(columnDefinition = "TEXT")
    private String observaciones;

}
