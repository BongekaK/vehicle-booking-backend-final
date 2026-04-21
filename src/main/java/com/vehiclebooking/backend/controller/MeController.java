package com.vehiclebooking.backend.controller;

import com.vehiclebooking.backend.dto.UserDto;
import com.vehiclebooking.backend.service.UserService;
import lombok.Data; // Added import
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;

    @GetMapping("/profile")
    public UserDto getMyProfile() {
        return userService.getAuthenticatedUser();
    }

    @PatchMapping("/password")
    public UserDto updateMyPassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
        return userService.updateUserPassword(updatePasswordRequest.getOldPassword(), updatePasswordRequest.getNewPassword());
    }

    // Inner class for password update request body
    @Data // Lombok annotation for getters, setters, equals, hashCode, toString
    public static class UpdatePasswordRequest {
        private String oldPassword;
        private String newPassword;
    }
}
