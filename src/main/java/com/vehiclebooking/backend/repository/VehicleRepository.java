package com.vehiclebooking.backend.repository;

import com.vehiclebooking.backend.entity.Vehicle;
import com.vehiclebooking.backend.entity.VehicleStatus; // Assuming this is the correct path for VehicleStatus
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    List<Vehicle> findByStatus(VehicleStatus status);
}
