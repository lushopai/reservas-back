package com.bms.reserva_servicio_backend.mappers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.bms.reserva_servicio_backend.models.Cabana;
import com.bms.reserva_servicio_backend.models.ItemReservado;
import com.bms.reserva_servicio_backend.models.PaqueteReserva;
import com.bms.reserva_servicio_backend.models.Recurso;
import com.bms.reserva_servicio_backend.models.Reserva;
import com.bms.reserva_servicio_backend.models.ServicioEntretencion;
import com.bms.reserva_servicio_backend.response.ItemReservadoResponse;
import com.bms.reserva_servicio_backend.response.PaqueteResponse;
import com.bms.reserva_servicio_backend.response.RecursoResponse;
import com.bms.reserva_servicio_backend.response.ReservaResponse;

@Component
public class ReservaMapper {
    
    /**
     * Convertir Reserva Entity a Response
     */
    public ReservaResponse toResponse(Reserva reserva) {
        // Obtener información del usuario
        String nombreUsuario = null;
        String emailUsuario = null;
        if (reserva.getUser() != null) {
            nombreUsuario = reserva.getUser().getNombres() + " " + reserva.getUser().getApellidos();
            emailUsuario = reserva.getUser().getEmail();
        }

        // Obtener nombre del recurso
        String nombreRecurso = reserva.getRecurso() != null ? reserva.getRecurso().getNombre() : null;

        return ReservaResponse.builder()
            .id(reserva.getId())
            .tipoReserva(reserva.getTipoReserva())
            .estado(reserva.getEstado())
            .fechaReserva(reserva.getFechaReserva())
            .fechaInicio(reserva.getFechaInicio())
            .fechaFin(reserva.getFechaFin())
            .precioBase(reserva.getPrecioBase())
            .precioItems(reserva.getPrecioItems())
            .precioTotal(reserva.getPrecioTotal())
            .nombreUsuario(nombreUsuario)
            .emailUsuario(emailUsuario)
            .nombreRecurso(nombreRecurso)
            .recurso(toRecursoResponse(reserva.getRecurso()))
            .itemsReservados(toItemsReservadosResponse(reserva.getItemsReservados()))
            .paqueteId(reserva.getPaquete() != null ? reserva.getPaquete().getId() : null)
            .nombrePaquete(reserva.getPaquete() != null ? reserva.getPaquete().getNombrePaquete() : null)
            .estadoPaquete(reserva.getPaquete() != null ? reserva.getPaquete().getEstado() : null)
            .observaciones(reserva.getObservaciones())
            .build();
    }
    
    /**
     * Convertir PaqueteReserva Entity a Response
     */
    public PaqueteResponse toPaqueteResponse(PaqueteReserva paquete) {
        long diasEstadia = ChronoUnit.DAYS.between(
            paquete.getFechaInicio().toLocalDate(),
            paquete.getFechaFin().toLocalDate()
        );
        
        double porcentajeDesc = paquete.getPrecioTotal().compareTo(BigDecimal.ZERO) > 0
            ? paquete.getDescuento().divide(paquete.getPrecioTotal(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).doubleValue()
            : 0.0;
        
        return PaqueteResponse.builder()
            .id(paquete.getId())
            .nombrePaquete(paquete.getNombrePaquete())
            .estado(paquete.getEstado())
            .fechaCreacion(paquete.getFechaCreacion())
            .fechaInicio(paquete.getFechaInicio())
            .fechaFin(paquete.getFechaFin())
            .precioTotal(paquete.getPrecioTotal())
            .descuento(paquete.getDescuento())
            .precioFinal(paquete.getPrecioFinal())
            .porcentajeDescuento(porcentajeDesc)
            .reservas(paquete.getReservas().stream()
                .map(this::toResponse)
                .collect(Collectors.toList()))
            .cantidadReservas(paquete.getReservas().size())
            .diasEstadia((int) diasEstadia)
            .notasEspeciales(paquete.getNotasEspeciales())
            .build();
    }
    
    
    private RecursoResponse toRecursoResponse(Recurso recurso) {
        // Obtener imagen principal
        String imagenPrincipal = recurso.getImagenes().stream()
            .filter(img -> img.getEsPrincipal())
            .findFirst()
            .map(img -> img.getUrl())
            .orElse(null);

        RecursoResponse.RecursoResponseBuilder builder = RecursoResponse.builder()
            .id(recurso.getId())
            .nombre(recurso.getNombre())
            .descripcion(recurso.getDescripcion())
            .estado(recurso.getEstado())
            .precioPorUnidad(recurso.getPrecioPorUnidad())
            .imagenes(recurso.getImagenes())
            .imagenPrincipalUrl(imagenPrincipal);

        if (recurso instanceof Cabana) {
            Cabana cabana = (Cabana) recurso;
            builder.tipo("CABAÑA")
                .capacidadPersonas(cabana.getCapacidadPersonas())
                .numeroHabitaciones(cabana.getNumeroHabitaciones())
                .tipoCabana(cabana.getTipoCabana());
        } else if (recurso instanceof ServicioEntretencion) {
            ServicioEntretencion servicio = (ServicioEntretencion) recurso;
            builder.tipo("SERVICIO")
                .tipoServicio(servicio.getTipoServicio())
                .duracionBloqueMinutos(servicio.getDuracionBloqueMinutos());
        }

        return builder.build();
    }
    
    private List<ItemReservadoResponse> toItemsReservadosResponse(List<ItemReservado> items) {
        return items.stream()
            .map(item -> ItemReservadoResponse.builder()
                .id(item.getId())
                .nombreItem(item.getItem().getNombre())
                .categoria(item.getItem().getCategoria())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(item.getSubtotal())
                .build())
            .collect(Collectors.toList());
    }
}