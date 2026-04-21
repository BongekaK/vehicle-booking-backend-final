package com.vehiclebooking.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.UUID;
import java.util.List;

import lombok.Data;

@Data
public class InspectionDto {

    private UUID id;
    private UUID bookingId;
    private String driverName;
    private String vehiclePlate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date inspectionDate;
    private List<InspectionItemDto> items;
    private Integer mileage;
}
