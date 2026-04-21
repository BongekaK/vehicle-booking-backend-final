package com.vehiclebooking.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vehiclebooking.backend.entity.UserRole;
import lombok.Data;
import java.util.UUID;

@Data
public class UserDto {

    private UUID id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserRole role;
    @JsonProperty("line_manager_id")
    private UUID lineManagerId;
    @JsonProperty("line_manager_name")
    private String lineManagerName;
    @JsonProperty("is_active")
    private boolean isActive;
    private boolean mustChangePassword;

    @JsonProperty("fullName")
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return null;
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
}
