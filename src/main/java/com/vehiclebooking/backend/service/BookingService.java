package com.vehiclebooking.backend.service;

import com.vehiclebooking.backend.dto.BookingManageDto;
import com.vehiclebooking.backend.dto.BookingRequestDto;
import com.vehiclebooking.backend.dto.BookingResponseDto;

import com.vehiclebooking.backend.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    BookingResponseDto createBooking(BookingRequestDto bookingRequestDto);
    BookingResponseDto createEmergencyBooking(BookingRequestDto bookingRequestDto);
    List<BookingResponseDto> getMyBookings(UUID userId);
    List<BookingManageDto> getManagedBookings(User authenticatedUser);
    BookingResponseDto approveBooking(UUID bookingId);
    BookingResponseDto allocateVehicle(UUID bookingId, String allocatedVehicleId);
    BookingResponseDto startTrip(UUID bookingId);
    BookingResponseDto returnVehicle(UUID bookingId);
    BookingResponseDto updateMileage(UUID bookingId, Integer mileage);
    BookingResponseDto updateInspectionStatus(UUID bookingId, Boolean status);
    BookingResponseDto changeAllocatedVehicle(UUID bookingId, String allocatedVehicleId);
    void cancelBooking(UUID bookingId);
    boolean isVehicleAvailable(String vehicleId, LocalDateTime startDate, LocalDateTime endDate, UUID bookingId);
    BookingResponseDto updateBooking(UUID bookingId, com.vehiclebooking.backend.dto.BookingUpdateDto bookingUpdateDto);
}
