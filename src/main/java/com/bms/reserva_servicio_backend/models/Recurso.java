package com.bms.reserva_servicio_backend.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.bms.reserva_servicio_backend.enums.EstadoRecurso;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "recursos", indexes = {
    @Index(name = "idx_recurso_estado", columnList = "estado"),
    @Index(name = "idx_recurso_tipo", columnList = "tipo_recurso"),
    @Index(name = "idx_recurso_nombre", columnList = "nombre")
})
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipo_recurso", discriminatorType = DiscriminatorType.STRING)
public abstract class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del recurso es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(length = 500)
    private String descripcion;

    @NotNull(message = "El estado del recurso es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRecurso estado;

    @NotNull(message = "El precio por unidad es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPorUnidad;

    @JsonIgnoreProperties("recurso")
    @OneToMany(mappedBy = "recurso", cascade = CascadeType.ALL)
    private List<ItemsInventario> inventario = new ArrayList<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "recurso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecursoImagen> imagenes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoRecurso getEstado() {
        return estado;
    }

    public void setEstado(EstadoRecurso estado) {
        this.estado = estado;
    }

    public BigDecimal getPrecioPorUnidad() {
        return precioPorUnidad;
    }

    public void setPrecioPorUnidad(BigDecimal precioPorUnidad) {
        this.precioPorUnidad = precioPorUnidad;
    }

    public List<ItemsInventario> getInventario() {
        return inventario;
    }

    public void setInventario(List<ItemsInventario> inventario) {
        this.inventario = inventario;
    }

    public List<RecursoImagen> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<RecursoImagen> imagenes) {
        this.imagenes = imagenes;
    }

    @Override
    public String toString() {
        return "Recurso [id=" + id + ", nombre=" + nombre + ", descripcion=" + descripcion + ", estado=" + estado
                + ", precioPorUnidad=" + precioPorUnidad + "]";
    }

   
    
}
