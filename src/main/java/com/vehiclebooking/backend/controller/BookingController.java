package com.vehiclebooking.backend.controller;

import org.springframework.security.core.Authentication;
import com.vehiclebooking.backend.dto.BookingManageDto;
import com.vehiclebooking.backend.dto.BookingRequestDto;
import com.vehiclebooking.backend.dto.BookingResponseDto;
import com.vehiclebooking.backend.service.BookingService;
import com.vehiclebooking.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize; // Added import
import java.util.Map;
import org.slf4j.Logger; // Added
import org.slf4j.LoggerFactory; // Added

import com.vehiclebooking.backend.dto.InspectionDto;
import com.vehiclebooking.backend.service.InspectionService;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;
    private final UserService userService;
    private final InspectionService inspectionService;

    @GetMapping("/{bookingId}/inspection")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FLEET_MANAGER', 'EMPLOYEE', 'SECURITY', 'OPS_OFFICIAL', 'LINE_MANAGER')")
    public ResponseEntity<InspectionDto> getInspectionByBookingId(@PathVariable UUID bookingId) {
        InspectionDto inspectionDto = inspectionService.getInspectionByBookingId(bookingId);
        if (inspectionDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(inspectionDto);
    }
    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(@RequestBody BookingRequestDto bookingRequestDto) {
        // Validation: Add logic to return a 400 Bad Request if requesterName, startDate, endDate, destination, or driverName are missing, or if policyAccepted is false.
        if (bookingRequestDto.getRequesterName() == null || bookingRequestDto.getRequesterName().isEmpty() ||
                bookingRequestDto.getStartDate() == null || bookingRequestDto.getEndDate() == null ||
                bookingRequestDto.getDestination() == null || bookingRequestDto.getDestination().isEmpty() ||
                bookingRequestDto.getDriverName() == null || bookingRequestDto.getDriverName().isEmpty() ||
                !bookingRequestDto.getPolicyAccepted()) { // Assuming policyAccepted is never null
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        BookingResponseDto newBooking = bookingService.createBooking(bookingRequestDto);
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }

    // B. User-Specific Retrieval (GET /bookings/my)
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponseDto>> getMyBookings(Authentication auth) {
        log.info("User: {} | Authorities: {}", auth.getName(), auth.getAuthorities());
        UUID userId = userService.getAuthenticatedUser().getId();
        List<BookingResponseDto> myBookings = bookingService.getMyBookings(userId);
        return ResponseEntity.ok(myBookings);
    }

    // C. Management Retrieval (GET /bookings/manage)
    @PreAuthorize("hasAnyAuthority('OPS_OFFICIAL', 'LINE_MANAGER', 'FLEET_MANAGER', 'ADMIN', 'SECURITY')")
    @GetMapping("/manage")
    public ResponseEntity<List<BookingManageDto>> getManagedBookings(Authentication auth) {
        // Need to get the authenticated user here to pass to the service layer for role-based filtering
        com.vehiclebooking.backend.entity.User authenticatedUser = userService.getAuthenticatedUserEntity();
        List<BookingManageDto> managedBookings = bookingService.getManagedBookings(authenticatedUser);
        return ResponseEntity.ok(managedBookings);
    }

    // D. Approval & Allocation (PATCH /bookings/:id/approve and /allocate)
    @PreAuthorize("hasAnyAuthority('OPS_OFFICIAL', 'FLEET_MANAGER') or (hasAuthority('LINE_MANAGER') and @bookingServiceImpl.isManagerOfBookingUser(#id, @userServiceImpl.getAuthenticatedUser().id))")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<BookingResponseDto> approveBooking(@PathVariable UUID id) {
        BookingResponseDto approvedBooking = bookingService.approveBooking(id);
        return ResponseEntity.ok(approvedBooking);
    }

    @PreAuthorize("hasAuthority('FLEET_MANAGER')")
    @PatchMapping("/{id}/allocate")
    public ResponseEntity<BookingResponseDto> allocateVehicle(
            @PathVariable UUID id,
            @RequestParam String vehiclePlate) {
        BookingResponseDto allocatedBooking = bookingService.allocateVehicle(id, vehiclePlate);
        return ResponseEntity.ok(allocatedBooking);
    }

    @PreAuthorize("hasAuthority('FLEET_MANAGER')")
    @PatchMapping("/{id}/change-vehicle")
    public ResponseEntity<BookingResponseDto> changeAllocatedVehicle(
            @PathVariable UUID id,
            @RequestParam String vehiclePlate) {
        BookingResponseDto changedBooking = bookingService.changeAllocatedVehicle(id, vehiclePlate);
        return ResponseEntity.ok(changedBooking);
    }

    // E. Trip Lifecycle Functions
    @PostMapping("/{id}/start-trip")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN', 'OPS_OFFICIAL', 'FLEET_MANAGER')")
    public ResponseEntity<BookingResponseDto> startTrip(@PathVariable UUID id) {
        BookingResponseDto startedTrip = bookingService.startTrip(id);
        return ResponseEntity.ok(startedTrip);
    }

    @PostMapping("/{id}/return-vehicle")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN', 'OPS_OFFICIAL', 'FLEET_MANAGER')")
    public ResponseEntity<BookingResponseDto> returnVehicle(@PathVariable UUID id) {
        BookingResponseDto returnedVehicle = bookingService.returnVehicle(id);
        return ResponseEntity.ok(returnedVehicle);
    }

    @PatchMapping("/{id}/mileage")
    public ResponseEntity<BookingResponseDto> updateMileage(@PathVariable UUID id, @RequestParam Integer mileage) {
        BookingResponseDto updatedBooking = bookingService.updateMileage(id, mileage);
        return ResponseEntity.ok(updatedBooking);
    }

    @PatchMapping("/{id}/inspection-status")
    public ResponseEntity<BookingResponseDto> updateInspectionStatus(@PathVariable UUID id, @RequestParam Boolean status) {
        BookingResponseDto updatedBooking = bookingService.updateInspectionStatus(id, status);
        return ResponseEntity.ok(updatedBooking);
    }

    @PostMapping("/emergency")
    public ResponseEntity<BookingResponseDto> emergencyBooking(@RequestBody BookingRequestDto bookingRequestDto) {
        BookingResponseDto newBooking = bookingService.createEmergencyBooking(bookingRequestDto);
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }

    // GET /api/vehicles (Placeholder - assuming VehicleController is not created yet)
    @GetMapping("/vehicles")
    public ResponseEntity<String> getVehicles() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Vehicle listing not yet implemented.");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FLEET_MANAGER', 'LINE_MANAGER', 'OPS_OFFICIAL') or @bookingServiceImpl.isOwnerOfBooking(#id, @userServiceImpl.getAuthenticatedUser().id)")
    public ResponseEntity<Void> cancelBooking(@PathVariable UUID id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FLEET_MANAGER', 'LINE_MANAGER', 'OPS_OFFICIAL') or @bookingServiceImpl.isOwnerOfBooking(#id, @userServiceImpl.getAuthenticatedUser().id)")
    public ResponseEntity<BookingResponseDto> updateBooking(@PathVariable UUID id, @RequestBody com.vehiclebooking.backend.dto.BookingUpdateDto bookingUpdateDto) {
        BookingResponseDto updatedBooking = bookingService.updateBooking(id, bookingUpdateDto);
        return ResponseEntity.ok(updatedBooking);
    }
}
