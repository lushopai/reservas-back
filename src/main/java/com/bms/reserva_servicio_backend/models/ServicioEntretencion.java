package com.bms.reserva_servicio_backend.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "servicios_entretencion", indexes = {
    @Index(name = "idx_servicio_tipo", columnList = "tipoServicio"),
    @Index(name = "idx_servicio_capacidad", columnList = "capacidadMaxima")
})
@PrimaryKeyJoinColumn(name = "recurso_id")
@DiscriminatorValue("SERVICIO")
public class ServicioEntretencion extends Recurso {

    @Column(length = 50)
    private String tipoServicio; // CANCHA_TENIS, PISCINA, QUINCHO, SPA, etc

    @NotNull(message = "La capacidad máxima es obligatoria")
    @Min(value = 1, message = "La capacidad máxima debe ser al menos 1")
    @Column(nullable = false)
    private Integer capacidadMaxima;

    @NotNull(message = "La duración del bloque es obligatoria")
    @Min(value = 15, message = "La duración mínima del bloque es 15 minutos")
    @Column(nullable = false)
    private Integer duracionBloqueMinutos; // 60 para 1 hora, 120 para 2 horas

    @Column(nullable = false)
    private Boolean requiereSupervision = false;

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
