package com.vehiclebooking.backend.dto;

import lombok.Data;

@Data
public class InspectionItemDto {

    private Long id;
    private String description;
    private String checkOut;
    private String checkIn;
}
