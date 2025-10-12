package com.bms.reserva_servicio_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bms.reserva_servicio_backend.models.Role;

public interface RoleRepository extends JpaRepository<Role,Long>{
    Optional<Role> findByName(String name);

}
