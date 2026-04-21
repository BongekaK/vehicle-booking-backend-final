package com.vehiclebooking.backend.service;

import com.vehiclebooking.backend.dto.InspectionDto;
import com.vehiclebooking.backend.entity.Booking;
import com.vehiclebooking.backend.entity.User;
import com.vehiclebooking.backend.repository.BookingRepository;
import com.vehiclebooking.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("customSecurityService")
@RequiredArgsConstructor
public class CustomSecurityService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @SuppressWarnings("null")
    public boolean isBookingOwner(InspectionDto inspectionDto) {
        String authenticatedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User authenticatedUser = userRepository.findByEmail(authenticatedUserEmail)
                .orElse(null);

        if (authenticatedUser == null) {
            return false;
        }

        Booking booking = bookingRepository.findById(inspectionDto.getBookingId())
                .orElse(null);

        return booking != null && booking.getUser().getId().equals(authenticatedUser.getId());
    }
}
