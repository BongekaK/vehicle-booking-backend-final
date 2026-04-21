package com.vehiclebooking.backend.service;

import com.vehiclebooking.backend.dto.VehicleDto;
import com.vehiclebooking.backend.entity.Vehicle;
import java.util.List;

public interface VehicleService {
    List<Vehicle> findAll();
    List<VehicleDto> getVehicles();
    VehicleDto getVehicle(String id);
    VehicleDto createVehicle(VehicleDto vehicleDto);
    VehicleDto updateVehicle(String id, VehicleDto vehicleDto);
    void deleteVehicle(String id);
    boolean getVehicleAvailability(String id, String startDate, String endDate);
}
