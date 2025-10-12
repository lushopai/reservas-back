package com.bms.reserva_servicio_backend.service;

import java.util.List;

import com.bms.reserva_servicio_backend.models.User;
import com.bms.reserva_servicio_backend.request.RegisterRequest;
import com.bms.reserva_servicio_backend.response.SuccessResponse;
import com.bms.reserva_servicio_backend.response.UserResponse;

public interface UserService {

    List<User> findAll();
    User save(User user);
    boolean existsByUsername(String username);

     // Nuevos métodos unificados
    SuccessResponse<UserResponse> register(RegisterRequest request);
    User findById(Long id);
    User findByEmail(String email);
    User findByDocumento(String documento);
    SuccessResponse<UserResponse> updateUser(Long id, RegisterRequest request);

    
}
