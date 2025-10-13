package com.bms.reserva_servicio_backend.controller;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bms.reserva_servicio_backend.models.User;
import com.bms.reserva_servicio_backend.request.RegisterRequest;
import com.bms.reserva_servicio_backend.request.UpdateUserRequest;
import com.bms.reserva_servicio_backend.response.SuccessResponse;
import com.bms.reserva_servicio_backend.response.UserResponse;
import com.bms.reserva_servicio_backend.service.UserService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * POST /api/users/register
     * Registrar nuevo usuario/cliente
     */
    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        SuccessResponse<UserResponse> response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/users/{id}
     * Obtener usuario por ID con estadísticas
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        User user = userService.findById(id);

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nombre(user.getNombres())
                .apellido(user.getApellidos())
                .documento(user.getDocumento())
                .tipoDocumento(user.getTipoDocumento())
                .email(user.getEmail())
                .telefono(user.getTelefono())
                .fechaRegistro(user.getFechaRegistro())
                .ultimoAcceso(user.getUltimoAcceso())
                .enabled(user.isEnabled())
                .totalReservas(user.getReservas().size())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/users/{id}
     * Actualizar datos del usuario
     */
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        SuccessResponse<UserResponse> response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/users
     * Listar todos los usuarios (admin)
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * PATCH /api/users/{id}/toggle-status
     * Cambiar estado del usuario (activar/desactivar)
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<SuccessResponse<UserResponse>> toggleUserStatus(@PathVariable Long id) {

        return ResponseEntity.ok(userService.toggleUserStatus(id));
    }

    /**
     * DELETE /api/users/{id}
     * Eliminar usuario (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<UserResponse>> deleteUser(@PathVariable Long id) {
        User user = userService.findById(id);
        user.setEnabled(false);
        userService.save(user);

        SuccessResponse<UserResponse> successResponse = new SuccessResponse<>();
        successResponse.setSuccess(true);
        successResponse.setTimestamp(LocalDateTime.now());
        successResponse.setMessage("Usuario eliminado exitosamente");

        return ResponseEntity.ok(successResponse);
    }

}
