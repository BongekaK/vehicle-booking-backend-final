package com.vehiclebooking.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vehiclebooking.backend.entity.BookingStatus;
import com.vehiclebooking.backend.entity.InspectionStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class BookingResponseDto {
    private UUID id;
    private UUID userId; // Link to the User who created the booking
    private String requesterName; // Full name of the person requesting the vehicle
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime requestDate; // The date the request was submitted
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime startDate; // Trip start date and time
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime endDate; // Trip end date and time
    private String destination; // Where the vehicle is going
    private String purpose; // Reason for the trip
    private Integer passengerCount; // Number of people in the vehicle
    private String driverName; // Full name of the designated driver
    private String driverLicense; // Driver's license number
    private String driverCell; // Driver's contact number (10 digits)
    private Boolean policyAccepted; // Whether the user agreed to terms
    private BookingStatus status; // PENDING, APPROVED, CANCELLED, COMPLETED
    private String vehicleRequired; // (Optional) The type of vehicle requested
    private VehicleDto allocatedVehicle;
    private String allocatedVehicleId;
    private String vehiclePlate;
    private String vehicleModel;
    private InspectionStatus inspectionStatus; // PENDING, COMPLETED, or OUT_DONE
    private Integer currentMileage; // The odometer reading upon return
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime actualStartTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime actualEndTime;
    private List<PassengerDto> passengers;
}
