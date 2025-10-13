package com.bms.reserva_servicio_backend.request;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class GenerarBloquesRequest {

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La hora de apertura es obligatoria")
    private LocalTime horaApertura;

    @NotNull(message = "La hora de cierre es obligatoria")
    private LocalTime horaCierre;

    @NotNull(message = "La duración del bloque es obligatoria")
    @Min(value = 15, message = "La duración mínima es 15 minutos")
    private Integer duracionBloqueMinutos;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(LocalTime horaApertura) {
        this.horaApertura = horaApertura;
    }

    public LocalTime getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(LocalTime horaCierre) {
        this.horaCierre = horaCierre;
    }

    public Integer getDuracionBloqueMinutos() {
        return duracionBloqueMinutos;
    }

    public void setDuracionBloqueMinutos(Integer duracionBloqueMinutos) {
        this.duracionBloqueMinutos = duracionBloqueMinutos;
    }
}
