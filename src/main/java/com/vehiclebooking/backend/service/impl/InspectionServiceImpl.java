package com.vehiclebooking.backend.service.impl;

import com.vehiclebooking.backend.dto.InspectionDto;
import com.vehiclebooking.backend.entity.Booking;
import com.vehiclebooking.backend.entity.Inspection;
import com.vehiclebooking.backend.entity.InspectionStatus;
import com.vehiclebooking.backend.entity.User;
import com.vehiclebooking.backend.entity.Vehicle;
import com.vehiclebooking.backend.repository.BookingRepository;
import com.vehiclebooking.backend.repository.InspectionRepository;
import com.vehiclebooking.backend.repository.UserRepository;
import com.vehiclebooking.backend.repository.VehicleRepository;
import com.vehiclebooking.backend.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectionServiceImpl implements InspectionService {

    private final InspectionRepository inspectionRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ModelMapper modelMapper;

    @SuppressWarnings("null")
    @Override
    @Transactional
    public InspectionDto saveInspection(InspectionDto inspectionDto) {
        // Fetch the booking to get vehicle details
        Booking booking = bookingRepository.findById(inspectionDto.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + inspectionDto.getBookingId()));

        if (booking.getAllocatedVehicle() == null) {
            throw new RuntimeException("Booking does not have an allocated vehicle for inspection.");
        }

        // Get the vehicle and update its mileage
        Vehicle vehicle = booking.getAllocatedVehicle();
        if (inspectionDto.getMileage() != null) {
            vehicle.setCurrentMileage(inspectionDto.getMileage());
            vehicleRepository.save(vehicle);
        }

        // Get the authenticated user (checked by)
        String authenticatedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User checkedBy = userRepository.findByEmail(authenticatedUserEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        // Find existing inspection by bookingId, or create a new one
        Inspection inspection = inspectionRepository.findByBookingId(booking.getId())
                .orElse(new Inspection());

        // Auto-populate fields
        inspection.setBooking(booking);
        inspection.setCheckedBy(checkedBy);
        inspection.setInspectionDate(new Date()); // Set current date/time for inspection

        // Clear existing items if updating, to prevent duplicates and handle changes
        if (inspection.getItems() != null) {
            inspection.getItems().clear();
        } else {
            inspection.setItems(new java.util.ArrayList<>());
        }

        // Map DTO items to entity items
        inspection.setItems(
                inspectionDto.getItems().stream()
                        .map(itemDto -> {
                            var item = modelMapper.map(itemDto, com.vehiclebooking.backend.entity.InspectionItem.class);
                            item.setInspection(inspection); // Link item back to inspection
                            return item;
                        })
                        .collect(Collectors.toList())
        );

        // Save inspection
        Inspection savedInspection = inspectionRepository.save(inspection);

        // Update booking inspection status
        booking.setInspectionStatus(InspectionStatus.COMPLETED); // Assuming pre-trip inspection marks it as completed
        bookingRepository.save(booking);

        // Populate DTO fields for response
        InspectionDto responseDto = modelMapper.map(savedInspection, InspectionDto.class);
        responseDto.setBookingId(booking.getId()); // Ensure bookingId is set in DTO
        responseDto.setVehiclePlate(booking.getAllocatedVehicle().getId()); // Add vehicle plate
        responseDto.setDriverName(
                (checkedBy.getFirstName() != null ? checkedBy.getFirstName() : "") +
                (checkedBy.getLastName() != null ? " " + checkedBy.getLastName() : "")
        );

        return responseDto;
    }

    @Override
    public InspectionDto getInspectionByBookingId(java.util.UUID bookingId) {
        return inspectionRepository.findByBookingId(bookingId)
                .map(inspection -> {
                    InspectionDto inspectionDto = modelMapper.map(inspection, InspectionDto.class);
                    inspectionDto.setDriverName(
                            (inspection.getCheckedBy().getFirstName() != null ? inspection.getCheckedBy().getFirstName() : "") +
                            (inspection.getCheckedBy().getLastName() != null ? " " + inspection.getCheckedBy().getLastName() : "")
                    );
                    if (inspection.getBooking().getAllocatedVehicle() != null) {
                        inspectionDto.setVehiclePlate(inspection.getBooking().getAllocatedVehicle().getId());
                    }
                    return inspectionDto;
                })
                .orElse(null);
    }
}
