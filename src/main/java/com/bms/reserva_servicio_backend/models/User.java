package com.bms.reserva_servicio_backend.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== DATOS DE AUTENTICACIÓN ==========
    @Column(unique = true, nullable = false)
    private String username;

    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;

    @Transient
    @JsonProperty(access = Access.WRITE_ONLY)
    private boolean admin;

    private boolean enabled;

    @JsonIgnoreProperties({ "users", "handler", "hibernateLazyInitializer" })
    @ManyToMany
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"), uniqueConstraints = {
            @UniqueConstraint(columnNames = { "user_id", "role_id" }) })
    private List<Role> roles = new ArrayList<>();

    // ========== DATOS DEL CLIENTE ==========
    private String nombres;
    private String apellidos;

    @Column(unique = true)
    private String email;

    private String telefono;

    @Column(unique = true)
    private String documento;

    private String tipoDocumento;
    private LocalDateTime fechaRegistro;

    // ========== RELACIONES (antes en Cliente) ==========
    @JsonIgnoreProperties({ "user", "handler", "hibernateLazyInitializer" })
    @OneToMany(mappedBy = "user")
    private List<Reserva> reservas = new ArrayList<>();

    @JsonIgnoreProperties({ "user", "handler", "hibernateLazyInitializer" })
    @OneToMany(mappedBy = "user")
    private List<PaqueteReserva> paquetesReserva = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        enabled = true;
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }

    }

    // Helper method para obtener nombre completo
    public String getNombreCompleto() {
        if (nombres != null && apellidos != null) {
            return nombres + " " + apellidos;
        }
        return nombres != null ? nombres : username;
    }
}
