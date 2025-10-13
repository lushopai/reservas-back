package com.bms.reserva_servicio_backend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bms.reserva_servicio_backend.models.Role;
import com.bms.reserva_servicio_backend.models.User;
import com.bms.reserva_servicio_backend.repository.RoleRepository;
import com.bms.reserva_servicio_backend.repository.UserRepository;
import com.bms.reserva_servicio_backend.request.RegisterRequest;
import com.bms.reserva_servicio_backend.request.UpdateUserRequest;
import com.bms.reserva_servicio_backend.response.SuccessResponse;
import com.bms.reserva_servicio_backend.response.UserResponse;
import com.bms.reserva_servicio_backend.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public User save(User user) {
        if (existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        Optional<Role> role = roleRepository.findByName("ROLE_USER");
        List<Role> roles = new ArrayList<>();
        role.ifPresent(roles::add);

        if (user.isAdmin()) {
            Optional<Role> adminRole = roleRepository.findByName("ROLE_ADMIN");
            adminRole.ifPresent(roles::add);
        }

        user.setRoles(roles);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    @Transactional
    public SuccessResponse<UserResponse> register(RegisterRequest request) {
        SuccessResponse<UserResponse> response = new SuccessResponse<>();

        // Validar email duplicado
        if (repository.existsByEmail(request.getEmail())) {
            response.setSuccess(false);
            response.setTimestamp(LocalDateTime.now());
            response.setMessage("El email ya está registrado: " + request.getEmail());
            return response;
        }

        // Validar documento duplicado
        if (repository.existsByDocumento(request.getDocumento())) {
            response.setSuccess(false);
            response.setTimestamp(LocalDateTime.now());
            response.setMessage("El documento ya está registrado: " + request.getDocumento());
            return response;
        }

        // Crear nuevo usuario
        User user = new User();
        user.setUsername(request.getEmail()); // usar email como username
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNombres(request.getNombre());
        user.setApellidos(request.getApellidos());  // Cambiar a getApellidos() plural
        user.setEmail(request.getEmail());
        user.setTelefono(request.getTelefono());
        user.setDocumento(request.getDocumento());
        user.setTipoDocumento(request.getTipoDocumento());
        user.setFechaRegistro(LocalDateTime.now());
        user.setEnabled(true);

        // Asignar rol USER por defecto
        Optional<Role> roleUser = roleRepository.findByName("ROLE_USER");
        List<Role> roles = new ArrayList<>();
        roleUser.ifPresent(roles::add);
        user.setRoles(roles);

        User savedUser = repository.save(user);

        // Construir respuesta
        UserResponse userResponse = UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .nombre(savedUser.getNombres())
                .apellidos(savedUser.getApellidos()) // Cambiar a apellidos (plural)
                .email(savedUser.getEmail())
                .telefono(savedUser.getTelefono())
                .documento(savedUser.getDocumento())
                .tipoDocumento(savedUser.getTipoDocumento())
                .fechaRegistro(savedUser.getFechaRegistro())
                .totalReservas(0)
                .build();

        response.setSuccess(true);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage("Usuario registrado exitosamente");
        response.setData(userResponse);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return repository.findByUsername(username)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return repository.findByEmail(email)
                .orElse(null);
    }

    @Override
    public User findByDocumento(String documento) {
        return repository.findByDocumento(documento)
                .orElse(null);
    }

    @Override
    @Transactional
    public SuccessResponse<UserResponse> updateUser(Long id, UpdateUserRequest request) {
        SuccessResponse<UserResponse> response = new SuccessResponse<>();

        User user = repository.findById(id).orElse(null);

        if (user == null) {
            response.setSuccess(false);
            response.setTimestamp(LocalDateTime.now());
            response.setMessage("Usuario no encontrado");
            return response;
        }

        // Validar email si cambió
        if (!user.getEmail().equals(request.getEmail())) {
            if (repository.existsByEmail(request.getEmail())) {
                response.setSuccess(false);
                response.setTimestamp(LocalDateTime.now());
                response.setMessage("El email ya está en uso");
                return response;
            }
            user.setEmail(request.getEmail());
            user.setUsername(request.getEmail());
        }

        // Validar documento si cambió
        if (!user.getDocumento().equals(request.getDocumento())) {
            if (repository.existsByDocumento(request.getDocumento())) {
                response.setSuccess(false);
                response.setTimestamp(LocalDateTime.now());
                response.setMessage("El documento ya está en uso");
                return response;
            }
            user.setDocumento(request.getDocumento());
        }

        // Actualizar datos
        user.setNombres(request.getNombre());
        user.setApellidos(request.getApellidos()); // Cambiar a getApellidos() plural
        user.setTelefono(request.getTelefono());
        user.setTipoDocumento(request.getTipoDocumento());

        // Actualizar contraseña solo si se proporcionó
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = repository.save(user);

        UserResponse userResponse = UserResponse.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .nombre(updatedUser.getNombres())
                .apellidos(updatedUser.getApellidos()) // Cambiar a apellidos (plural)
                .email(updatedUser.getEmail())
                .telefono(updatedUser.getTelefono())
                .documento(updatedUser.getDocumento())
                .tipoDocumento(updatedUser.getTipoDocumento())
                .build();

        response.setSuccess(true);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage("Usuario actualizado exitosamente");
        response.setData(userResponse);

        return response;
    }

    @Override
    @Transactional
    public void updateUltimoAcceso(String username) {
        repository.findByUsername(username).ifPresent(user -> {
            user.setUltimoAcceso(LocalDateTime.now());
            repository.save(user);
        });
    }

    @Override
    public SuccessResponse<UserResponse> toggleUserStatus(Long id) {
        SuccessResponse<UserResponse> successResponse = new SuccessResponse<>();

        User user = repository.findById(id).orElse(null);
        if (user == null) {
            successResponse.setSuccess(false);
            successResponse.setTimestamp(LocalDateTime.now());
            successResponse.setData(null);
            successResponse.setMessage("Error: usuario no existe");
            return successResponse;
        }

        user.setEnabled(!user.isEnabled());
        repository.save(user);
        successResponse.setSuccess(true);
        successResponse.setTimestamp(LocalDateTime.now());
        successResponse.setData(null);
        successResponse.setMessage("OK");
        return successResponse;

    }

}
