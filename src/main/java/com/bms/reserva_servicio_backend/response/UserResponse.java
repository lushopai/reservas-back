package com.bms.reserva_servicio_backend.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String nombre;
    private String apellido;
    private String documento;
    private String tipoDocumento;
    private String email;
    private String telefono;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoAcceso;
    private Boolean enabled;
    private Integer totalReservas;
}