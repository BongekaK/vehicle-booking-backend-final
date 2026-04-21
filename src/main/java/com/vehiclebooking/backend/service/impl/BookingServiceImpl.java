package com.vehiclebooking.backend.service.impl;

import com.vehiclebooking.backend.dto.BookingManageDto;
import com.vehiclebooking.backend.dto.BookingRequestDto;
import com.vehiclebooking.backend.dto.BookingResponseDto;
import com.vehiclebooking.backend.entity.Booking;
import com.vehiclebooking.backend.entity.BookingStatus;
import com.vehiclebooking.backend.entity.InspectionStatus;
import com.vehiclebooking.backend.entity.Passenger;
import com.vehiclebooking.backend.entity.User;
import com.vehiclebooking.backend.repository.BookingRepository;
import com.vehiclebooking.backend.repository.UserRepository;
import com.vehiclebooking.backend.service.AuditService;
import com.vehiclebooking.backend.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import com.vehiclebooking.backend.entity.Vehicle;
import com.vehiclebooking.backend.repository.VehicleRepository;
import com.vehiclebooking.backend.entity.UserRole;
import com.vehiclebooking.backend.entity.VehicleStatus; // Added import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class); // Added

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AuditService auditService; // Assuming AuditService exists and is used for logging actions
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto bookingRequestDto) {
        String authenticatedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + authenticatedUserEmail));

        // Conflict Checking: Before saving a booking, the backend should check if the allocatedVehicleId is already booked for the requested startDate and endDate.
        if (bookingRequestDto.getAllocatedVehicleId() != null) {
            boolean isConflicting = bookingRepository.hasConflictingBookings(bookingRequestDto.getAllocatedVehicleId(), bookingRequestDto.getStartDate(), bookingRequestDto.getEndDate(), UUID.randomUUID());
            if (isConflicting) {
                throw new RuntimeException("Vehicle " + bookingRequestDto.getAllocatedVehicleId() + " is unavailable for this booking period.");
            }
        }


        Booking booking = modelMapper.map(bookingRequestDto, Booking.class);
        booking.setUser(user);
        booking.setRequestDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.PENDING);
        booking.setInspectionStatus(InspectionStatus.PENDING); // Initial inspection status

        List<Passenger> passengers = bookingRequestDto.getPassengers().stream()
                .map(passengerDto -> {
                    Passenger passenger = modelMapper.map(passengerDto, Passenger.class);
                    passenger.setBooking(booking); // Link passenger to booking
                    return passenger;
                }).collect(Collectors.toList());
        booking.setPassengers(passengers);

        Booking savedBooking = bookingRepository.save(booking);
        auditService.logAction("CREATE_BOOKING", "Booking created by " + authenticatedUserEmail + " for destination " + booking.getDestination());
        return toBookingResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto createEmergencyBooking(BookingRequestDto bookingRequestDto) {
        String authenticatedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + authenticatedUserEmail));

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2); // Emergency booking for 2 hours

        List<Vehicle> availableVehicles = vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);

        Vehicle availableVehicle = availableVehicles.stream()
                .filter(vehicle -> !bookingRepository.hasConflictingBookings(vehicle.getId(), startTime, endTime, UUID.randomUUID()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No available vehicle for emergency booking."));

        Booking booking = modelMapper.map(bookingRequestDto, Booking.class);
        booking.setUser(user);
        booking.setRequestDate(startTime);
        booking.setStartDate(startTime);
        booking.setEndDate(endTime);
        booking.setStatus(BookingStatus.ALLOCATED);
        booking.setAllocatedVehicle(availableVehicle);
        booking.setInspectionStatus(InspectionStatus.PENDING);
        booking.setRequesterName(user.getFirstName() + " " + user.getLastName());
        booking.setDestination(bookingRequestDto.getDestination());

        if (bookingRequestDto.getPassengers() != null) {
            List<Passenger> passengers = bookingRequestDto.getPassengers().stream()
                    .map(passengerDto -> {
                        Passenger passenger = modelMapper.map(passengerDto, Passenger.class);
                        passenger.setBooking(booking);
                        return passenger;
                    }).collect(Collectors.toList());
            booking.setPassengers(passengers);
        }

        Booking savedBooking = bookingRepository.save(booking);
        auditService.logAction("CREATE_EMERGENCY_BOOKING", "Emergency booking created by " + authenticatedUserEmail);
        return toBookingResponseDto(savedBooking);
    }


    @Override
    public List<BookingResponseDto> getMyBookings(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("User ID cannot be null");
        List<Booking> myBookings = bookingRepository.findByUser_Id(userId);
        return myBookings.stream()
                .map(this::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingManageDto> getManagedBookings(User authenticatedUser) {
        UUID authenticatedUserId = authenticatedUser.getId();

        List<Booking> managedBookings;

        if (authenticatedUser.getRole() == UserRole.OPS_OFFICIAL || authenticatedUser.getRole() == UserRole.FLEET_MANAGER) {
            // OPS_OFFICIAL and FLEET_MANAGER see all non-CANCELLED or COMPLETED bookings
            managedBookings = bookingRepository.findAllByStatusNotIn(List.of(BookingStatus.CANCELLED, BookingStatus.COMPLETED));
        } else if (authenticatedUser.getRole() == UserRole.LINE_MANAGER) {
            // LINE_MANAGER sees bookings for users who report to them
            List<UUID> directReportIds = userRepository.findByLineManagerId(authenticatedUserId).stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            managedBookings = bookingRepository.findByUser_IdIn(directReportIds);
        } else if (authenticatedUser.getRole() == UserRole.SECURITY) {
            // SECURITY sees only bookings with an allocated vehicle that are not CANCELLED or COMPLETED
            managedBookings = bookingRepository.findByAllocatedVehicleNotNullAndStatusNotIn(List.of(BookingStatus.CANCELLED, BookingStatus.COMPLETED));
        }
        else {
            // Other roles (or no role) see no managed bookings
            managedBookings = List.of();
        }

        return managedBookings.stream()
                .map(booking -> {
                    BookingManageDto dto = modelMapper.map(booking, BookingManageDto.class);
                    if (booking.getAllocatedVehicle() != null) {
                        dto.setAllocatedVehicle(modelMapper.map(booking.getAllocatedVehicle(), com.vehiclebooking.backend.dto.VehicleDto.class));
                        dto.setAllocatedVehicleId(booking.getAllocatedVehicle().getId());
                        dto.setVehiclePlate(booking.getAllocatedVehicle().getId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(UUID bookingId) {
        if (bookingId == null) throw new IllegalArgumentException("Booking ID cannot be null");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        booking.setStatus(BookingStatus.APPROVED);
        Booking updatedBooking = bookingRepository.save(booking);
        auditService.logAction("APPROVE_BOOKING", "Booking " + bookingId + " approved.");
        return toBookingResponseDto(updatedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto allocateVehicle(UUID bookingId, String allocatedVehicleId) {
        log.info("BookingServiceImpl.allocateVehicle: Authentication: {}", SecurityContextHolder.getContext().getAuthentication()); // Added log

        if (bookingId == null) throw new IllegalArgumentException("Booking ID cannot be null");
        if (allocatedVehicleId == null) throw new IllegalArgumentException("Allocated vehicle ID cannot be null");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.IN_PROGRESS || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot change allocated vehicle for a booking that has already started or is completed.");
        }
        
        Vehicle vehicle = vehicleRepository.findById(allocatedVehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + allocatedVehicleId));

        // Conflict Checking for allocation
        boolean isConflicting = bookingRepository.hasConflictingBookings(allocatedVehicleId, booking.getStartDate(), booking.getEndDate(), bookingId);
        if (isConflicting) {
            throw new RuntimeException("Vehicle " + vehicle.getId() + " is unavailable for this booking period.");
        }

        booking.setAllocatedVehicle(vehicle);
        booking.setStatus(BookingStatus.ALLOCATED);
        Booking updatedBooking = bookingRepository.save(booking);
        auditService.logAction("ALLOCATE_VEHHICLE", "Vehicle " + allocatedVehicleId + " allocated to booking " + bookingId);
        return toBookingResponseDto(updatedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto startTrip(UUID bookingId) {
        if (bookingId == null) throw new IllegalArgumentException("Booking ID cannot be null");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        if (booking.getActualStartTime() != null) {
            throw new IllegalStateException("Trip already started for booking: " + bookingId);
        }
        
        String authenticatedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + authenticatedUserEmail));

        // Management roles (OPS_OFFICIAL, FLEET_MANAGER, ADMIN) can bypass inspection check
        boolean isManagementRole = currentUser.getRole() == UserRole.OPS_OFFICIAL || 
                                 currentUser.getRole() == UserRole.FLEET_MANAGER || 
                                 currentUser.getRole() == UserRole.ADMIN;

        if (!isManagementRole && booking.getInspectionStatus() != InspectionStatus.COMPLETED) {
            throw new IllegalStateException("Inspection must be 'COMPLETED' to start trip for booking: " + bookingId);
        }

        booking.setActualStartTime(LocalDateTime.now());
        
        Vehicle vehicle = booking.getAllocatedVehicle();
        if (vehicle == null) {
            throw new IllegalStateException("No vehicle allocated for booking: " + bookingId);
        }
        vehicle.setStatus(VehicleStatus.IN_TRANSIT);
        vehicleRepository.save(vehicle);

        booking.setStatus(BookingStatus.IN_PROGRESS);
        Booking updatedBooking = bookingRepository.save(booking);
        auditService.logAction("START_TRIP", "Trip started for booking " + bookingId);
        return toBookingResponseDto(updatedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto returnVehicle(UUID bookingId) {
        if (bookingId == null) throw new IllegalArgumentException("Booking ID cannot be null");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        if (booking.getActualEndTime() != null) {
            throw new IllegalStateException("Trip already returned for booking: " + bookingId);
        }

        booking.setActualEndTime(LocalDateTime.now());

        Vehicle vehicle = booking.getAllocatedVehicle();
        if (vehicle == null) {
            throw new IllegalStateException("No vehicle allocated for booking: " + bookingId);
        }
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);

        booking.setStatus(BookingStatus.COMPLETED);
        Booking updatedBooking = bookingRepository.save(booking);
        auditService.logAction("RETURN_VEHICLE", "Vehicle returned for booking " + bookingId);
        return toBookingResponseDto(updatedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateMileage(UUID bookingId, Integer mileage) {
        if (bookingId == null) throw new IllegalArgumentException("Booking ID cannot be null");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        booking.setCurrentMileage(mileage);
        Booking updatedBooking = bookingRepository.save(booking);
        auditService.logAction("UPDATE_MILEAGE", "Mileage updated for booking " + bookingId + " to " + mileage);
        return toBookingResponseDto(updatedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateInspectionStatus(UUID bookingId, Boolean status) {
        if (bookingId == null) throw new IllegalArgumentException("Booking ID cannot be null");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        booking.setInspectionStatus(status ? InspectionStatus.COMPLETED : InspectionStatus.OUT_DONE);
        Booking updatedBooking = bookingRepository.save(booking);
        auditService.logAction("UPDATE_INSPECTION_STATUS", "Inspection status updated for booking " + bookingId + " to " + (status ? "APPROVED" : "REJECTED"));
        return toBookingResponseDto(updatedBooking);
    }

    // Helper method for @PreAuthorize to check if the authenticated manager is the line manager of the booking's user
    public boolean isManagerOfBookingUser(UUID bookingId, UUID managerId) {
        if (bookingId == null) throw new IllegalArgumentException("Booking ID cannot be null");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        User bookingUser = booking.getUser();
        return bookingUser != null && bookingUser.getLineManagerId() != null && bookingUser.getLineManagerId().equals(managerId);
    }

    @Override
    @Transactional
    public BookingResponseDto changeAllocatedVehicle(UUID bookingId, String allocatedVehicleId) {
        if (bookingId == null) throw new IllegalArgumentException("Booking ID cannot be null");
        if (allocatedVehicleId == null) throw new IllegalArgumentException("Allocated vehicle ID cannot be null");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.IN_PROGRESS || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot change allocated vehicle for a booking that has already started or is completed.");
        }

        Vehicle vehicle = vehicleRepository.findById(allocatedVehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found: " + allocatedVehicleId));

        // Conflict Checking for allocation
        boolean isConflicting = bookingRepository.hasConflictingBookings(allocatedVehicleId, booking.getStartDate(), booking.getEndDate(), bookingId);
        if (isConflicting) {
            throw new RuntimeException("Vehicle " + vehicle.getId() + " is unavailable for this booking period.");
        }

        booking.setAllocatedVehicle(vehicle);
        // Status remains unchanged for vehicle change
        Booking updatedBooking = bookingRepository.save(booking);
        auditService.logAction("CHANGE_VEHICLE", "Vehicle for booking " + bookingId + " changed to " + allocatedVehicleId);
        return toBookingResponseDto(updatedBooking);
    }

    public boolean isOwnerOfBooking(UUID bookingId, UUID userId) {
        if (bookingId == null) throw new IllegalArgumentException("Booking ID cannot be null");
        return bookingRepository.findById(bookingId)
                .map(Booking::getUser)
                .map(User::getId)
                .map(id -> id.equals(userId))
                .orElse(false);
    }

    @Override
    @Transactional
    public void cancelBooking(UUID bookingId) {
        if (bookingId == null) throw new IllegalArgumentException("Booking ID cannot be null");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.IN_PROGRESS || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a booking that has already started or is completed.");
        }

        if (booking.getAllocatedVehicle() != null) {
            Vehicle vehicle = booking.getAllocatedVehicle();
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        auditService.logAction("CANCEL_BOOKING", "Booking " + bookingId + " cancelled.");
    }

    @Override
    public boolean isVehicleAvailable(String vehicleId, LocalDateTime startDate, LocalDateTime endDate, UUID bookingId) {
        if (bookingId == null) {
            bookingId = UUID.randomUUID();
        }
        boolean hasConflict = bookingRepository.hasConflictingBookings(vehicleId, startDate, endDate, bookingId);
        log.info("Availability Check: Vehicle={}, Start={}, End={}, BookingID={}, HasConflict={}", 
                 vehicleId, startDate, endDate, bookingId, hasConflict);
        return !hasConflict;
    }

    private BookingResponseDto toBookingResponseDto(Booking booking) {
        BookingResponseDto dto = modelMapper.map(booking, BookingResponseDto.class);
        if (booking.getAllocatedVehicle() != null) {
            dto.setAllocatedVehicle(modelMapper.map(booking.getAllocatedVehicle(), com.vehiclebooking.backend.dto.VehicleDto.class));
            dto.setAllocatedVehicleId(booking.getAllocatedVehicle().getId().toLowerCase());
            dto.setVehiclePlate(booking.getAllocatedVehicle().getId().toLowerCase());
            dto.setVehicleModel(booking.getAllocatedVehicle().getModel());
        } else {
            dto.setAllocatedVehicleId(null);
        }
        return dto;
    }

    @Override
    @Transactional
    public BookingResponseDto updateBooking(UUID bookingId, com.vehiclebooking.backend.dto.BookingUpdateDto bookingUpdateDto) {
        if (bookingId == null) throw new IllegalArgumentException("Booking ID cannot be null");
        try {
            Booking existingBooking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Booking not found: " + bookingId));

            modelMapper.map(bookingUpdateDto, existingBooking);
            Booking updatedBooking = bookingRepository.save(Objects.requireNonNull(existingBooking));
            return toBookingResponseDto(updatedBooking);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Data integrity violation", e);
        }
    }
}
