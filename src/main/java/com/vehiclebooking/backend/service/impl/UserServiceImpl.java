package com.vehiclebooking.backend.service.impl;

import java.util.UUID;

import com.vehiclebooking.backend.dto.UserDto;
import com.vehiclebooking.backend.dto.ManagerOptionDto;
import com.vehiclebooking.backend.entity.User;
import com.vehiclebooking.backend.entity.UserRole;
import com.vehiclebooking.backend.repository.UserRepository;
import com.vehiclebooking.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder; // Changed import
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.vehiclebooking.backend.service.AuditService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder; // Changed type
    private final AuditService auditService;

    @Override
    public UserDto getUser(UUID id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = modelMapper.map(userDto, User.class);
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        if (user.getRole() == null) {
            user.setRole(UserRole.EMPLOYEE);
        }
        user.setActive(true);
        user.setMustChangePassword(true);

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto updateUser(UUID id, UserDto userDto) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setEmail(userDto.getEmail());
        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setRole(userDto.getRole());
        existingUser.setActive(userDto.isActive());
        existingUser.setMustChangePassword(userDto.isMustChangePassword());
        existingUser.setLineManagerId(userDto.getLineManagerId());

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public void deleteUser(UUID id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto toggleUserStatus(UUID id) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean newStatus = !existingUser.isActive();
        existingUser.setActive(newStatus);
        User updatedUser = userRepository.save(existingUser);
        
        String action = newStatus ? "ADMIN_UNLOCK_ACCOUNT" : "ADMIN_LOCK_ACCOUNT";
        String details = (newStatus ? "Admin unlocked account: " : "Admin locked account: ") + updatedUser.getEmail();
        auditService.logAction(action, details);
        
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public UserDto toggleUserStatusByEmail(String email) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        boolean newStatus = !existingUser.isActive();
        existingUser.setActive(newStatus);
        User updatedUser = userRepository.save(existingUser);
        
        String action = newStatus ? "ADMIN_UNLOCK_ACCOUNT" : "ADMIN_LOCK_ACCOUNT";
        String details = (newStatus ? "Admin unlocked account: " : "Admin locked account: ") + updatedUser.getEmail();
        auditService.logAction(action, details);
        
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public UserDto resetUserPassword(UUID id, String newPassword) {
        if (id == null) throw new IllegalArgumentException("ID cannot be null");
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setMustChangePassword(true);
        User updatedUser = userRepository.save(existingUser);
        auditService.logAction("RESET_PASSWORD", "User " + updatedUser.getEmail() + " password reset.");
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public UserDto resetUserPasswordByEmail(String email, String newPassword) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setMustChangePassword(true);
        User updatedUser = userRepository.save(existingUser);
        auditService.logAction("RESET_PASSWORD", "User " + updatedUser.getEmail() + " password reset.");
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public List<ManagerOptionDto> getAllManagers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.FLEET_MANAGER || user.getRole() == UserRole.LINE_MANAGER)
                .map(user -> new ManagerOptionDto(user.getId(), user.getFirstName() + " " + user.getLastName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getDirectReports(UUID managerId) {
        return userRepository.findByLineManagerId(managerId).stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            User user = userRepository.findByEmail("admin@alteram.co.za")
                    .orElse(userRepository.findAll().get(0));
            return modelMapper.map(user, UserDto.class);
        }

        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User getAuthenticatedUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return userRepository.findByEmail("admin@alteram.co.za")
                    .orElse(userRepository.findAll().get(0));
        }

        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    @Override
    public UserDto updateUserPassword(String oldPassword, String newPassword) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect old password.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        User updatedUser = userRepository.save(user);
        auditService.logAction("UPDATE_OWN_PASSWORD", "User " + updatedUser.getEmail() + " updated own password.");
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public UserDto forcePasswordReset(UUID userId, String newTemporaryPassword) {
        if (userId == null) throw new IllegalArgumentException("User ID cannot be null");
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setPassword(passwordEncoder.encode(newTemporaryPassword));
        existingUser.setMustChangePassword(true);
        User updatedUser = userRepository.save(existingUser);
        auditService.logAction("FORCE_PASSWORD_RESET", "Admin force reset password for user " + updatedUser.getEmail());
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public void forcePasswordReset(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("User ID cannot be null");
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setMustChangePassword(true);
        userRepository.save(existingUser);
        auditService.logAction("FORCE_PASSWORD_RESET_FLAG", "Admin flagged user " + existingUser.getEmail() + " to change password on next login.");
    }

    @Override
    public List<UserDto> bulkCreateUsers(List<UserDto> users) {
        if (users == null) throw new IllegalArgumentException("User DTOs cannot be null");
        return users.stream()
                .map(this::createUser)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto assignLineManager(UUID userId, UUID lineManagerId) {
        if (userId == null) throw new IllegalArgumentException("User ID cannot be null");
        User userToUpdate = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Optional: Validate if lineManagerId corresponds to an existing user with a manager role
        if (lineManagerId != null) {
        }
        
        userToUpdate.setLineManagerId(lineManagerId);
        User updatedUser = userRepository.save(userToUpdate);
        auditService.logAction("ASSIGN_LINE_MANAGER", "Admin assigned line manager (ID: " + lineManagerId + ") to user (ID: " + userId + ")");
        return modelMapper.map(updatedUser, UserDto.class);
    }
}

