package com.bms.reserva_servicio_backend.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bms.reserva_servicio_backend.models.RecursoImagen;
import com.bms.reserva_servicio_backend.response.SuccessResponse;
import com.bms.reserva_servicio_backend.service.RecursoImagenService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/recursos/{recursoId}/imagenes")
public class RecursoImagenController {

    @Autowired
    private RecursoImagenService imagenService;

    /**
     * POST /api/recursos/{recursoId}/imagenes
     * Subir imagen para un recurso
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SuccessResponse<RecursoImagen>> subirImagen(
            @PathVariable Long recursoId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) Boolean esPrincipal) {

        try {
            RecursoImagen imagen = imagenService.subirImagen(recursoId, file, descripcion, esPrincipal);

            SuccessResponse<RecursoImagen> response = new SuccessResponse<>();
            response.setSuccess(true);
            response.setMessage("Imagen subida exitosamente");
            response.setData(imagen);
            response.setTimestamp(LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            SuccessResponse<RecursoImagen> response = new SuccessResponse<>();
            response.setSuccess(false);
            response.setMessage("Error al subir la imagen: " + e.getMessage());
            response.setTimestamp(LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * GET /api/recursos/{recursoId}/imagenes
     * Obtener todas las imágenes de un recurso
     */
    @GetMapping
    public ResponseEntity<List<RecursoImagen>> obtenerImagenes(@PathVariable Long recursoId) {
        List<RecursoImagen> imagenes = imagenService.obtenerImagenesPorRecurso(recursoId);
        return ResponseEntity.ok(imagenes);
    }

    /**
     * GET /api/recursos/{recursoId}/imagenes/principal
     * Obtener imagen principal de un recurso
     */
    @GetMapping("/principal")
    public ResponseEntity<RecursoImagen> obtenerImagenPrincipal(@PathVariable Long recursoId) {
        RecursoImagen imagen = imagenService.obtenerImagenPrincipal(recursoId);
        if (imagen != null) {
            return ResponseEntity.ok(imagen);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * DELETE /api/recursos/{recursoId}/imagenes/{imagenId}
     * Eliminar imagen
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{imagenId}")
    public ResponseEntity<SuccessResponse<Void>> eliminarImagen(
            @PathVariable Long recursoId,
            @PathVariable Long imagenId) {

        try {
            imagenService.eliminarImagen(imagenId);

            SuccessResponse<Void> response = new SuccessResponse<>();
            response.setSuccess(true);
            response.setMessage("Imagen eliminada exitosamente");
            response.setTimestamp(LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            SuccessResponse<Void> response = new SuccessResponse<>();
            response.setSuccess(false);
            response.setMessage("Error al eliminar la imagen: " + e.getMessage());
            response.setTimestamp(LocalDateTime.now());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * PATCH /api/recursos/{recursoId}/imagenes/{imagenId}/principal
     * Establecer imagen como principal
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{imagenId}/principal")
    public ResponseEntity<SuccessResponse<RecursoImagen>> establecerPrincipal(
            @PathVariable Long recursoId,
            @PathVariable Long imagenId) {

        RecursoImagen imagen = imagenService.establecerImagenPrincipal(imagenId);

        SuccessResponse<RecursoImagen> response = new SuccessResponse<>();
        response.setSuccess(true);
        response.setMessage("Imagen establecida como principal");
        response.setData(imagen);
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/recursos/{recursoId}/imagenes/{imagenId}/orden
     * Actualizar orden de visualización
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{imagenId}/orden")
    public ResponseEntity<SuccessResponse<Void>> actualizarOrden(
            @PathVariable Long recursoId,
            @PathVariable Long imagenId,
            @RequestParam Integer orden) {

        imagenService.actualizarOrden(imagenId, orden);

        SuccessResponse<Void> response = new SuccessResponse<>();
        response.setSuccess(true);
        response.setMessage("Orden actualizado exitosamente");
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
