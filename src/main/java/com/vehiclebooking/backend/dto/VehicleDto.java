package com.vehiclebooking.backend.dto;

import com.vehiclebooking.backend.entity.VehicleStatus; // Add this import
import lombok.Data;

@Data
public class VehicleDto {

    private String id;
    private String make;
    private String model;
    private String plate;
    private VehicleStatus status; // Changed type to VehicleStatus
    private Integer currentMileage;
}
