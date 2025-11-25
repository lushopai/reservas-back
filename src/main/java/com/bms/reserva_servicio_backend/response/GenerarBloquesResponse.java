package com.bms.reserva_servicio_backend.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta detallada al generar bloques horarios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerarBloquesResponse {
    private Integer bloquesCreados;        // Cantidad de bloques nuevos creados
    private Integer bloquesDuplicados;     // Cantidad de bloques que ya existían
    private Integer bloquesTotalesGenerados; // Total de intentos de creación
    private String mensaje;                 // Mensaje descriptivo para el usuario
    private Boolean exitoso;                // Si la operación fue exitosa
    private String detalles;               // Detalles adicionales (ej: "Verifique que no existan reservas en esas horas")
    private String horaAperturaReal;       // Hora de apertura real utilizada (respetando horarios del servicio)
    private String horaCierreReal;         // Hora de cierre real utilizada (respetando horarios del servicio)
}
