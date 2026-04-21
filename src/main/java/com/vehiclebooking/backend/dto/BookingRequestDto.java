package com.vehiclebooking.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingRequestDto {
    private String requesterName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String destination;
    private String purpose;
    private Integer passengerCount;
    private String driverName;
    private String driverLicense;
    private String driverCell;
    private Boolean policyAccepted;
    private String vehicleRequired;
    private String allocatedVehicleId; // Added for conflict checking
    private List<PassengerDto> passengers;
}
