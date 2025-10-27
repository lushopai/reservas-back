package com.bms.reserva_servicio_backend.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Clase base para entidades que soportan eliminación lógica (soft delete)
 * En lugar de eliminar físicamente el registro, se marca como deleted=true
 *
 * IMPORTANTE: Las entidades que extiendan esta clase deben agregar las anotaciones:
 * - @SQLDelete(sql = "UPDATE nombre_tabla SET deleted = true, deleted_date = NOW() WHERE id = ?")
 * - @Where(clause = "deleted = false")
 * (Requiere: org.hibernate.annotations.SQLDelete y org.hibernate.annotations.Where)
 */
@MappedSuperclass
@Getter
@Setter
public abstract class SoftDeletable extends Auditable {

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_date")
    private LocalDateTime deletedDate;

    @Column(name = "deleted_by")
    private String deletedBy;

    /**
     * Marca la entidad como eliminada (soft delete)
     */
    public void markAsDeleted(String deletedBy) {
        this.deleted = true;
        this.deletedDate = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * Restaura una entidad eliminada
     */
    public void restore() {
        this.deleted = false;
        this.deletedDate = null;
        this.deletedBy = null;
    }

    /**
     * Verifica si la entidad está eliminada
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }
}
