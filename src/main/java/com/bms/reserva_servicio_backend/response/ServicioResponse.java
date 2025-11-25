package com.bms.reserva_servicio_backend.response;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import com.bms.reserva_servicio_backend.models.RecursoImagen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precioPorUnidad;
    private String estado; // DISPONIBLE, MANTENIMIENTO, FUERA_SERVICIO

    // Características específicas del servicio
    private String tipoServicio; // CANCHA_TENIS, PISCINA, QUINCHO, SPA, etc
    private Integer capacidadMaxima;
    private Integer duracionBloqueMinutos;
    private Boolean requiereSupervision;
    
    // Horarios
    private LocalTime horaApertura;
    private LocalTime horaCierre;

    // Información adicional
    private Integer totalReservas; // Total de reservas históricas
    private Boolean disponibleHoy; // Si está disponible hoy
    private Integer bloquesDisponibles; // Cantidad de bloques horarios configurados
    private Integer itemsInventario; // Cantidad de items asociados (equipamiento)

    // Imágenes
    private List<RecursoImagen> imagenes;

}
