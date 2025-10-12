package com.bms.reserva_servicio_backend.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "recursos")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private String estado; // DISPONIBLE, MANTENIMIENTO, FUERA_SERVICIO
    private BigDecimal precioPorUnidad;

    @OneToMany(mappedBy = "recurso", cascade = CascadeType.ALL)
    private List<ItemsInventario> inventario = new ArrayList<>();

    @OneToMany(mappedBy = "recurso")
    private List<Reserva> reservas = new ArrayList<>();

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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
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

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    @Override
    public String toString() {
        return "Recurso [id=" + id + ", nombre=" + nombre + ", descripcion=" + descripcion + ", estado=" + estado
                + ", precioPorUnidad=" + precioPorUnidad + ", inventario=" + inventario + ", reservas=" + reservas
                + "]";
    }

   
    
}
