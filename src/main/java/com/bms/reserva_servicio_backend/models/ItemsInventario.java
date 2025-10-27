package com.bms.reserva_servicio_backend.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.bms.reserva_servicio_backend.enums.EstadoItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "items_inventario", indexes = {
    @Index(name = "idx_item_recurso", columnList = "recurso_id"),
    @Index(name = "idx_item_categoria", columnList = "categoria"),
    @Index(name = "idx_item_estado", columnList = "estadoItem"),
    @Index(name = "idx_item_reservable", columnList = "esReservable")
})
public class ItemsInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El recurso es obligatorio")
    @JsonIgnoreProperties({"inventario", "imagenes"})
    @ManyToOne
    @JoinColumn(name = "recurso_id", nullable = false)
    private Recurso recurso;

    @NotBlank(message = "El nombre del item es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre; // TV, Cama, Raqueta, Pelota

    @Column(length = 50)
    private String categoria; // MOBILIARIO, ELECTRODOMESTICO, EQUIPAMIENTO_DEPORTIVO

    @NotNull(message = "La cantidad total es obligatoria")
    @Min(value = 0, message = "La cantidad total no puede ser negativa")
    @Column(nullable = false)
    private Integer cantidadTotal;

    @NotNull(message = "La cantidad disponible es obligatoria")
    @Min(value = 0, message = "La cantidad disponible no puede ser negativa")
    @Column(nullable = false)
    private Integer cantidadDisponible;

    @NotNull(message = "El estado del item es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoItem estadoItem;

    @NotNull(message = "El campo esReservable es obligatorio")
    @Column(nullable = false)
    private Boolean esReservable; // true si se puede reservar individualmente

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    @Column(precision = 10, scale = 2)
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

    public EstadoItem getEstadoItem() {
        return estadoItem;
    }

    public void setEstadoItem(EstadoItem estadoItem) {
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
