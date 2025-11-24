package com.bms.reserva_servicio_backend.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.bms.reserva_servicio_backend.dto.ItemReservaDTO;
import com.bms.reserva_servicio_backend.dto.PaqueteReservaDTO;
import com.bms.reserva_servicio_backend.dto.ServicioReservaDTO;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaqueteReservaRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotBlank(message = "El nombre del paquete es obligatorio")
    @Size(min = 5, max = 100, message = "El nombre debe tener entre 5 y 100 caracteres")
    private String nombre;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio; // Cambiar a LocalDate para recibir solo fecha

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin; // Cambiar a LocalDate para recibir solo fecha

    // Cabaña (opcional, puede ser un paquete solo de servicios)
    private Long cabanaId;
    private List<ItemReservaDTO> itemsAdicionales;

    // Servicios incluidos (opcional, puede ser solo cabaña)
    private List<ServicioReservaDTO> servicios;

    private String notasEspeciales;

    /**
     * Convertir Request a DTO interno
     */
    public PaqueteReservaDTO toDTO() {
        PaqueteReservaDTO dto = new PaqueteReservaDTO();
        dto.setNombre(this.nombre);
        // Convertir LocalDate a LocalDateTime (check-in a las 15:00, check-out a las
        // 12:00)
        dto.setFechaInicio(this.fechaInicio.atTime(15, 0));
        dto.setFechaFin(this.fechaFin.atTime(12, 0));
        dto.setCabanaId(this.cabanaId);
        dto.setItemsCabana(this.itemsAdicionales);
        dto.setServicios(this.servicios);
        dto.setNotasEspeciales(this.notasEspeciales);
        return dto;
    }

    /**
     * Validación personalizada
     */
    @AssertTrue(message = "La fecha de fin debe ser posterior a la fecha de inicio")
    public boolean isFechasValidas() {
        if (fechaInicio == null || fechaFin == null) {
            return true; // @NotNull se encarga
        }
        return fechaFin.isAfter(fechaInicio);
    }

    @AssertTrue(message = "Debe incluir al menos una cabaña o un servicio")
    public boolean tieneAlMenosUnRecurso() {
        return cabanaId != null || (servicios != null && !servicios.isEmpty());
    }
}