package com.bms.reserva_servicio_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "cabanas")
@PrimaryKeyJoinColumn(name = "recurso_id")
public class Cabana extends Recurso {

    private Integer capacidadPersonas;
    private Integer numeroHabitaciones;
    private Integer numeroBanos;
    private Double metrosCuadrados;
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
