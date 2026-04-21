package com.vehiclebooking.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "bookings")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Link to the User who created the booking

    private String requesterName; // Full name of the person requesting the vehicle
    private LocalDateTime requestDate; // The date the request was submitted
    private LocalDateTime startDate; // Trip start date and time
    private LocalDateTime endDate; // Trip end date and time
    private String destination; // Where the vehicle is going
    private String purpose; // Reason for the trip
    private Integer passengerCount; // Number of people in the vehicle
    private String driverName; // Full name of the designated driver
    private String driverLicense; // Driver's license number
    private String driverCell; // Driver's contact number (10 digits)
    private Boolean policyAccepted; // Whether the user agreed to terms

    @Enumerated(EnumType.STRING)
    private BookingStatus status; // PENDING, APPROVED, CANCELLED, COMPLETED

    private String vehicleRequired; // (Optional) The type of vehicle requested
    @ManyToOne
    @JoinColumn(name = "allocated_vehicle_id")
    private Vehicle allocatedVehicle; // The actual vehicle assigned by Fleet Manager

    @Enumerated(EnumType.STRING)
    private InspectionStatus inspectionStatus; // PENDING, COMPLETED, or OUT_DONE
    private Integer currentMileage; // The odometer reading upon return

    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Passenger> passengers;
}
