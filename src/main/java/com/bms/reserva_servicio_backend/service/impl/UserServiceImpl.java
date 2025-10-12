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
                .email(savedUser.getEmail())
                .telefono(savedUser.getTelefono())
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
    public SuccessResponse<UserResponse> updateUser(Long id, RegisterRequest request) {
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

        // Actualizar datos
        user.setNombres(request.getNombre());
        user.setTelefono(request.getTelefono());

        User updatedUser = repository.save(user);

        UserResponse userResponse = UserResponse.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .nombre(updatedUser.getNombres())
                .email(updatedUser.getEmail())
                .telefono(updatedUser.getTelefono())
                .build();

        response.setSuccess(true);
        response.setTimestamp(LocalDateTime.now());
        response.setMessage("Usuario actualizado exitosamente");
        response.setData(userResponse);

        return response;
    }





}
