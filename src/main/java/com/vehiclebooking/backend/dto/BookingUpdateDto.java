package com.vehiclebooking.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.vehiclebooking.backend.entity.BookingStatus;
import com.vehiclebooking.backend.entity.InspectionStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingUpdateDto {
    private String requesterName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String destination;
    private String driverName;
    private Boolean policyAccepted;
    private BookingStatus status;
    private UUID allocatedVehicleId;
    private InspectionStatus inspectionStatus;
    private Integer currentMileage;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private List<PassengerDto> passengers;
}
