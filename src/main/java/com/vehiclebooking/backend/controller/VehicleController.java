package com.vehiclebooking.backend.controller;

import com.vehiclebooking.backend.dto.VehicleDto;
import com.vehiclebooking.backend.service.VehicleService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public ResponseEntity<List<VehicleDto>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getVehicles());
    }

    @GetMapping("/{id}")
    public VehicleDto getVehicle(@PathVariable String id) {
        return vehicleService.getVehicle(id);
    }

    @PostMapping
    public VehicleDto createVehicle(@RequestBody VehicleDto vehicleDto) {
        return vehicleService.createVehicle(vehicleDto);
    }

    @PutMapping("/{id}")
    public VehicleDto updateVehicle(@PathVariable String id, @RequestBody VehicleDto vehicleDto) {
        return vehicleService.updateVehicle(id, vehicleDto);
    }

    @DeleteMapping("/{id}")
    public void deleteVehicle(@PathVariable String id) {
        vehicleService.deleteVehicle(id);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<Boolean> getVehicleAvailability(@PathVariable String id, @RequestParam String startDate, @RequestParam String endDate) {
        if (id.equals("undefined")) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(vehicleService.getVehicleAvailability(id, startDate, endDate));
    }
}
