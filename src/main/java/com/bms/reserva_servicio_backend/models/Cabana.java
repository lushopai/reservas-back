package com.bms.reserva_servicio_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "cabanas", indexes = {
    @Index(name = "idx_cabana_capacidad", columnList = "capacidadPersonas"),
    @Index(name = "idx_cabana_tipo", columnList = "tipoCabana")
})
@PrimaryKeyJoinColumn(name = "recurso_id")
@DiscriminatorValue("CABANA")
public class Cabana extends Recurso {

    @NotNull(message = "La capacidad de personas es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1 persona")
    @Column(nullable = false)
    private Integer capacidadPersonas;

    @Min(value = 0, message = "El número de habitaciones no puede ser negativo")
    private Integer numeroHabitaciones;

    @Min(value = 0, message = "El número de baños no puede ser negativo")
    private Integer numeroBanos;

    @Min(value = 0, message = "Los metros cuadrados no pueden ser negativos")
    private Double metrosCuadrados;

    @Column(length = 50)
    private String tipoCabana;

    @Column(columnDefinition = "TEXT")
    private String serviciosIncluidos;

    public Integer getCapacidadPersonas() {
        return capacidadPersonas;
    }

    public void setCapacidadPersonas(Integer capacidadPersonas) {
        this.capacidadPersonas = capacidadPersonas;
    }

    public Integer getNumeroHabitaciones() {
        return numeroHabitaciones;
    }

    public void setNumeroHabitaciones(Integer numeroHabitaciones) {
        this.numeroHabitaciones = numeroHabitaciones;
    }

    public Integer getNumeroBanos() {
        return numeroBanos;
    }

    public void setNumeroBanos(Integer numeroBanos) {
        this.numeroBanos = numeroBanos;
    }

    public Double getMetrosCuadrados() {
        return metrosCuadrados;
    }

    public void setMetrosCuadrados(Double metrosCuadrados) {
        this.metrosCuadrados = metrosCuadrados;
    }

    public String getTipoCabana() {
        return tipoCabana;
    }

    public void setTipoCabana(String tipoCabana) {
        this.tipoCabana = tipoCabana;
    }

    public String getServiciosIncluidos() {
        return serviciosIncluidos;
    }

    public void setServiciosIncluidos(String serviciosIncluidos) {
        this.serviciosIncluidos = serviciosIncluidos;
    }

    @Override
    public String toString() {
        return "Cabana [capacidadPersonas=" + capacidadPersonas + ", numeroHabitaciones=" + numeroHabitaciones
                + ", numeroBanos=" + numeroBanos + ", metrosCuadrados=" + metrosCuadrados + ", tipoCabana=" + tipoCabana
                + ", serviciosIncluidos=" + serviciosIncluidos + "]";
    }

}
