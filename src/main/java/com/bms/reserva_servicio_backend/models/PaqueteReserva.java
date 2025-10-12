package com.bms.reserva_servicio_backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@Table(name = "paquetes_reserva")
@Data
public class PaqueteReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
   
    @JsonIgnoreProperties({ "paquetesReserva", "reservas", "password", "roles", "handler", "hibernateLazyInitializer" })
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String nombrePaquete;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    @OneToMany(mappedBy = "paquete")
    private List<Reserva> reservas = new ArrayList<>();

    private String estado;

    private BigDecimal precioTotal;
    private BigDecimal descuento;
    private BigDecimal precioFinal;

    @Column(columnDefinition = "TEXT")
    private String notasEspeciales;

}
