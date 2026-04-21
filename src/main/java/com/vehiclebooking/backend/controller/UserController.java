package com.vehiclebooking.backend.controller;

import com.vehiclebooking.backend.dto.UserDto;
import com.vehiclebooking.backend.dto.ManagerOptionDto;
import com.vehiclebooking.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping; // Added for PATCH
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor // Added for constructor injection
public class UserController {

    private final UserService userService;

    // @PreAuthorize("hasAnyAuthority('ADMIN', 'SECURITY', 'FLEET_MANAGER')")
    @GetMapping // GET /api/users
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/managers") // GET /api/users/managers
    public List<ManagerOptionDto> getManagers() {
        return userService.getAllManagers();
    }

    @GetMapping("/email/{email}") // GET /api/users/email/{email}
    public UserDto getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @PatchMapping("/email/{email}/password") // PATCH /api/users/email/{email}/password
    public UserDto resetUserPasswordByEmail(@PathVariable String email, @RequestBody ResetPasswordRequest resetPasswordRequest) {
        return userService.resetUserPasswordByEmail(email, resetPasswordRequest.getNewPassword());
    }

    @GetMapping("/reports/{managerId}") // GET /api/users/reports/{managerId}
    public List<UserDto> getDirectReports(@PathVariable UUID managerId) {
        return userService.getDirectReports(managerId);
    }

    @PatchMapping("/{id}/toggle-active") // PATCH /api/users/{id}/toggle-active
    public UserDto toggleUserStatus(@PathVariable UUID id) {
        return userService.toggleUserStatus(id);
    }

    @PatchMapping("/{id}/password") // PATCH /api/users/{id}/password
    public UserDto resetUserPassword(@PathVariable UUID id, @RequestBody String newPassword) {
        return userService.resetUserPassword(id, newPassword);
    }

    @PatchMapping("/{id}/force-password-reset")
    public void forcePasswordReset(@PathVariable UUID id) {
        userService.forcePasswordReset(id);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PatchMapping("/{id}/assign-line-manager")
    public UserDto assignLineManager(@PathVariable UUID id, @RequestBody AssignLineManagerRequest request) {
        return userService.assignLineManager(id, request.getLineManagerId());
    }

    @PatchMapping("/email/{email}/toggle-active") // PATCH /api/users/email/{email}/toggle-active
    public UserDto toggleUserStatusByEmail(@PathVariable String email) {
        return userService.toggleUserStatusByEmail(email);
    }

    @GetMapping("/{id}") // GET /api/users/{id}
    public UserDto getUser(@PathVariable UUID id) {
        return userService.getUser(id);
    }

    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable UUID id, @RequestBody UserDto userDto) {
        return userService.updateUser(id, userDto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
    }

    @lombok.Data
    public static class ResetPasswordRequest {
        private String newPassword;
    }

    @lombok.Data
    public static class AssignLineManagerRequest {
        private UUID lineManagerId;
    }
}
