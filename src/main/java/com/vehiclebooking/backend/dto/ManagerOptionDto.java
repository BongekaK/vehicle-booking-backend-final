package com.vehiclebooking.backend.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ManagerOptionDto {
    private UUID id;
    private String name; // firstName + " " + lastName

    public ManagerOptionDto(UUID id, String name) {
        this.id = id;
        this.name = name;
    }
}
