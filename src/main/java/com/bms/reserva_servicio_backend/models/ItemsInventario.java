package com.bms.reserva_servicio_backend.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "items_inventario")
public class ItemsInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({"inventario", "imagenes", "reservas"})
    @ManyToOne
    @JoinColumn(name = "recurso_id")
    private Recurso recurso;

    private String nombre; // TV, Cama, Raqueta, Pelota
    private String categoria; // MOBILIARIO, ELECTRODOMESTICO, EQUIPAMIENTO_DEPORTIVO
    private Integer cantidadTotal;
    private Integer cantidadDisponible;
    private String estadoItem; // NUEVO, BUENO, REGULAR, REPARACION
    private Boolean esReservable; // true si se puede reservar individualmente
    private BigDecimal precioReserva; // precio si es reservable

    @OneToMany(mappedBy = "item")
    private List<ItemReservado> reservas = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Recurso getRecurso() {
        return recurso;
    }

    public void setRecurso(Recurso recurso) {
        this.recurso = recurso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getCantidadTotal() {
        return cantidadTotal;
    }

    public void setCantidadTotal(Integer cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
    }

    public Integer getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(Integer cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }

    public String getEstadoItem() {
        return estadoItem;
    }

    public void setEstadoItem(String estadoItem) {
        this.estadoItem = estadoItem;
    }

    public Boolean getEsReservable() {
        return esReservable;
    }

    public void setEsReservable(Boolean esReservable) {
        this.esReservable = esReservable;
    }

    public BigDecimal getPrecioReserva() {
        return precioReserva;
    }

    public void setPrecioReserva(BigDecimal precioReserva) {
        this.precioReserva = precioReserva;
    }

    public List<ItemReservado> getReservas() {
        return reservas;
    }

    public void setReservas(List<ItemReservado> reservas) {
        this.reservas = reservas;
    }

    @Override
    public String toString() {
        return "ItemsInventario [id=" + id + ", recurso=" + recurso + ", nombre=" + nombre + ", categoria=" + categoria
                + ", cantidadTotal=" + cantidadTotal + ", cantidadDisponible=" + cantidadDisponible + ", estadoItem="
                + estadoItem + ", esReservable=" + esReservable + ", precioReserva=" + precioReserva + ", reservas="
                + reservas + "]";
    }

    
    

}
