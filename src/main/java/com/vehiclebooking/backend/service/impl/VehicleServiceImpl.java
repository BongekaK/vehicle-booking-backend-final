package com.vehiclebooking.backend.service.impl;

import com.vehiclebooking.backend.dto.VehicleDto;
import com.vehiclebooking.backend.entity.Vehicle;
import com.vehiclebooking.backend.repository.VehicleRepository;
import com.vehiclebooking.backend.service.AuditService;
import com.vehiclebooking.backend.service.VehicleService;
import com.vehiclebooking.backend.service.BookingService;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ModelMapper modelMapper;
    private final AuditService auditService;
    private final BookingService bookingService;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, ModelMapper modelMapper, AuditService auditService, BookingService bookingService) {
        this.vehicleRepository = vehicleRepository;
        this.modelMapper = modelMapper;
        this.auditService = auditService;
        this.bookingService = bookingService;
    }

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    @Override
    public List<VehicleDto> getVehicles() {
        return vehicleRepository.findAll()
                .stream()
                .map(vehicle -> modelMapper.map(vehicle, VehicleDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public VehicleDto getVehicle(String id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        return modelMapper.map(vehicle, VehicleDto.class);
    }

    @Override
    public VehicleDto createVehicle(VehicleDto vehicleDto) {
        if (vehicleDto == null) throw new IllegalArgumentException("VehicleDto cannot be null");
        Vehicle vehicle = modelMapper.map(vehicleDto, Vehicle.class);
        @SuppressWarnings("null")
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return modelMapper.map(savedVehicle, VehicleDto.class);
    }

    @Override
    public VehicleDto updateVehicle(String id, VehicleDto vehicleDto) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        if (vehicleDto == null) throw new IllegalArgumentException("VehicleDto cannot be null");
        Vehicle existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        modelMapper.map(vehicleDto, existingVehicle);
        @SuppressWarnings("null")
        Vehicle updatedVehicle = vehicleRepository.save(existingVehicle);
        return modelMapper.map(updatedVehicle, VehicleDto.class);
    }

    @Override
    public void deleteVehicle(String id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        auditService.logAction("DELETE_VEHICLE", "Vehicle with id " + id + " has been deleted.");
        vehicleRepository.deleteById(id);
    }

    @Override
    public boolean getVehicleAvailability(String id, String startDate, String endDate) {
        java.time.ZonedDateTime start = java.time.ZonedDateTime.parse(startDate);
        java.time.ZonedDateTime end = java.time.ZonedDateTime.parse(endDate);
        
        // Convert to System Default (Africa/Johannesburg) to match database storage
        java.time.LocalDateTime localStart = start.withZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime();
        java.time.LocalDateTime localEnd = end.withZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime();
        
        return bookingService.isVehicleAvailable(id, localStart, localEnd, null);
    }
}
