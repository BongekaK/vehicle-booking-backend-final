package com.vehiclebooking.backend.service;

import com.vehiclebooking.backend.dto.InspectionDto;

public interface InspectionService {
    InspectionDto saveInspection(InspectionDto inspectionDto);
    InspectionDto getInspectionByBookingId(java.util.UUID bookingId);
}
