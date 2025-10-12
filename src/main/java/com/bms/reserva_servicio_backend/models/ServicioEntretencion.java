package com.bms.reserva_servicio_backend.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "servicios_entretencion")
@PrimaryKeyJoinColumn(name = "recurso_id")
public class ServicioEntretencion extends Recurso {

    private String tipoServicio; // CANCHA_TENIS, PISCINA, QUINCHO, SPA, etc
    private Integer capacidadMaxima;
    private Integer duracionBloqueMinutos; // 60 para 1 hora, 120 para 2 horas
    private Boolean requiereSupervision;

    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL)
    private List<BloqueHorario> bloquesDisponibles = new ArrayList<>();

    public String getTipoServicio() {
        return tipoServicio;
    }

    public void setTipoServicio(String tipoServicio) {
        this.tipoServicio = tipoServicio;
    }

    public Integer getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public Integer getDuracionBloqueMinutos() {
        return duracionBloqueMinutos;
    }

    public void setDuracionBloqueMinutos(Integer duracionBloqueMinutos) {
        this.duracionBloqueMinutos = duracionBloqueMinutos;
    }

    public Boolean getRequiereSupervision() {
        return requiereSupervision;
    }

    public void setRequiereSupervision(Boolean requiereSupervision) {
        this.requiereSupervision = requiereSupervision;
    }

    public List<BloqueHorario> getBloquesDisponibles() {
        return bloquesDisponibles;
    }

    public void setBloquesDisponibles(List<BloqueHorario> bloquesDisponibles) {
        this.bloquesDisponibles = bloquesDisponibles;
    }

    @Override
    public String toString() {
        return "ServicioEntretencion [tipoServicio=" + tipoServicio + ", capacidadMaxima=" + capacidadMaxima
                + ", duracionBloqueMinutos=" + duracionBloqueMinutos + ", requiereSupervision=" + requiereSupervision
                + ", bloquesDisponibles=" + bloquesDisponibles + "]";
    }

    
    
}
