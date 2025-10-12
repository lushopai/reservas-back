package com.bms.reserva_servicio_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bms.reserva_servicio_backend.models.User;

public interface UserRepository extends JpaRepository<User,Long> {
    // Métodos de autenticación
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    
    // Métodos de cliente
    Optional<User> findByEmail(String email);
    Optional<User> findByDocumento(String documento);
    boolean existsByEmail(String email);
    boolean existsByDocumento(String documento);




}
