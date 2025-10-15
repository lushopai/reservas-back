package com.bms.reserva_servicio_backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bms.reserva_servicio_backend.models.Recurso;
import com.bms.reserva_servicio_backend.models.RecursoImagen;
import com.bms.reserva_servicio_backend.repository.RecursoImagenRepository;
import com.bms.reserva_servicio_backend.repository.RecursoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class RecursoImagenService {

    @Autowired
    private RecursoImagenRepository imagenRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Value("${app.upload.dir:uploads/recursos}")
    private String uploadDir;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Subir imagen para un recurso
     */
    @Transactional
    public RecursoImagen subirImagen(Long recursoId, MultipartFile file, String descripcion, Boolean esPrincipal)
            throws IOException {

        Recurso recurso = recursoRepository.findById(recursoId)
                .orElseThrow(() -> new EntityNotFoundException("Recurso no encontrado con id: " + recursoId));

        // Validar archivo
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Validar tipo de archivo (solo imágenes)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen");
        }

        // Crear directorio si no existe
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generar nombre único para el archivo
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;

        // Guardar archivo
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Si es imagen principal, quitar flag de otras imágenes
        if (esPrincipal != null && esPrincipal) {
            List<RecursoImagen> imagenesExistentes = imagenRepository.findByRecursoIdOrderByOrdenVisualizacionAsc(recursoId);
            for (RecursoImagen img : imagenesExistentes) {
                img.setEsPrincipal(false);
            }
            imagenRepository.saveAll(imagenesExistentes);
        }

        // Obtener el siguiente orden de visualización
        List<RecursoImagen> imagenes = imagenRepository.findByRecursoIdOrderByOrdenVisualizacionAsc(recursoId);
        int nextOrden = imagenes.isEmpty() ? 0 : imagenes.get(imagenes.size() - 1).getOrdenVisualizacion() + 1;

        // Crear registro en BD
        RecursoImagen imagen = RecursoImagen.builder()
                .recurso(recurso)
                .url(baseUrl + "/uploads/recursos/" + filename)
                .nombre(originalFilename)
                .descripcion(descripcion)
                .esPrincipal(esPrincipal != null ? esPrincipal : false)
                .ordenVisualizacion(nextOrden)
                .fechaSubida(LocalDateTime.now())
                .build();

        return imagenRepository.save(imagen);
    }

    /**
     * Obtener todas las imágenes de un recurso
     */
    public List<RecursoImagen> obtenerImagenesPorRecurso(Long recursoId) {
        return imagenRepository.findByRecursoIdOrderByOrdenVisualizacionAsc(recursoId);
    }

    /**
     * Obtener imagen principal de un recurso
     */
    public RecursoImagen obtenerImagenPrincipal(Long recursoId) {
        return imagenRepository.findByRecursoIdAndEsPrincipalTrue(recursoId);
    }

    /**
     * Eliminar imagen
     */
    @Transactional
    public void eliminarImagen(Long imagenId) throws IOException {
        RecursoImagen imagen = imagenRepository.findById(imagenId)
                .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada con id: " + imagenId));

        // Extraer filename de la URL
        String url = imagen.getUrl();
        String filename = url.substring(url.lastIndexOf("/") + 1);

        // Eliminar archivo del sistema de archivos
        Path filePath = Paths.get(uploadDir).resolve(filename);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // Eliminar registro de BD
        imagenRepository.delete(imagen);
    }

    /**
     * Establecer imagen como principal
     */
    @Transactional
    public RecursoImagen establecerImagenPrincipal(Long imagenId) {
        RecursoImagen imagen = imagenRepository.findById(imagenId)
                .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada con id: " + imagenId));

        // Quitar flag de otras imágenes del mismo recurso
        List<RecursoImagen> imagenes = imagenRepository.findByRecursoIdOrderByOrdenVisualizacionAsc(imagen.getRecurso().getId());
        for (RecursoImagen img : imagenes) {
            img.setEsPrincipal(false);
        }
        imagenRepository.saveAll(imagenes);

        // Establecer esta como principal
        imagen.setEsPrincipal(true);
        return imagenRepository.save(imagen);
    }

    /**
     * Actualizar orden de visualización
     */
    @Transactional
    public void actualizarOrden(Long imagenId, Integer nuevoOrden) {
        RecursoImagen imagen = imagenRepository.findById(imagenId)
                .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada con id: " + imagenId));

        imagen.setOrdenVisualizacion(nuevoOrden);
        imagenRepository.save(imagen);
    }
}
