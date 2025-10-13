package com.bms.reserva_servicio_backend.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String email;

    // Password es opcional en actualizaciones
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Teléfono inválido")
    private String telefono;

    @NotBlank(message = "El documento es obligatorio")
    private String documento;

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento; // RUT, DNI, PASAPORTE

}
