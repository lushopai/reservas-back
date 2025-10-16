package com.bms.reserva_servicio_backend.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "recursos_imagenes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecursoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "recurso_id", nullable = false)
    private Recurso recurso;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 255)
    private String nombre; // Nombre original del archivo

    @Column(length = 100)
    private String descripcion;

    @Builder.Default
    @Column(name = "es_principal")
    private Boolean esPrincipal = false; // Imagen principal del recurso

    @Builder.Default
    @Column(name = "orden_visualizacion")
    private Integer ordenVisualizacion = 0;

    @Column(name = "fecha_subida")
    private LocalDateTime fechaSubida;

    @PrePersist
    protected void onCreate() {
        if (fechaSubida == null) {
            fechaSubida = LocalDateTime.now();
        }
    }
}
