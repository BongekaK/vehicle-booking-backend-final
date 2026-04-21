package com.vehiclebooking.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuditLogDto {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Action is required")
    private String action;

    @NotBlank(message = "Details are required")
    private String details;
}
