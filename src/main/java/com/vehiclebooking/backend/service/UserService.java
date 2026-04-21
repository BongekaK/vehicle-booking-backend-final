package com.vehiclebooking.backend.service;

import com.vehiclebooking.backend.dto.UserDto;
import com.vehiclebooking.backend.dto.ManagerOptionDto;
import com.vehiclebooking.backend.entity.User;
import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDto getUser(UUID id);
    UserDto getUserByEmail(String email);
    UserDto createUser(UserDto userDto);
    UserDto updateUser(UUID id, UserDto userDto);
    void deleteUser(UUID id);
    List<UserDto> getAllUsers();
    UserDto toggleUserStatus(UUID id);
    UserDto toggleUserStatusByEmail(String email);
    UserDto resetUserPassword(UUID id, String newPassword);
    UserDto resetUserPasswordByEmail(String email, String newPassword);
    List<ManagerOptionDto> getAllManagers();
    List<UserDto> getDirectReports(UUID managerId);
    UserDto getAuthenticatedUser();
    User getAuthenticatedUserEntity();
    UserDto updateUserPassword(String oldPassword, String newPassword);
    UserDto forcePasswordReset(UUID userId, String newTemporaryPassword);
    void forcePasswordReset(UUID userId);
    List<UserDto> bulkCreateUsers(List<UserDto> users);
    UserDto assignLineManager(UUID userId, UUID lineManagerId);
}
