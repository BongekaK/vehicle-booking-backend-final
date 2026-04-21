package com.vehiclebooking.backend.service.impl;

import com.vehiclebooking.backend.dto.AuditLogDto; // Added import
import com.vehiclebooking.backend.dto.UserDto;
import com.vehiclebooking.backend.entity.AuditEntry;
import com.vehiclebooking.backend.repository.AuditRepository;
import com.vehiclebooking.backend.service.AuditService;
import com.vehiclebooking.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditRepository auditRepository;

    private final UserService userService;

    public AuditServiceImpl(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    public void logAction(String action, String details) {
        UserDto user = userService.getAuthenticatedUser();
        String username = user.getEmail();
        String role = user.getRole().toString();

        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setUsername(username);
        auditEntry.setRole(role);
        auditEntry.setAction(action);
        auditEntry.setDetails(details);
        auditRepository.save(auditEntry);
    }

    @Override
    public void logAction(AuditLogDto auditLogDto) {
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setUsername(auditLogDto.getUsername());
        auditEntry.setRole(auditLogDto.getRole());
        auditEntry.setAction(auditLogDto.getAction());
        auditEntry.setDetails(auditLogDto.getDetails());
        auditRepository.save(auditEntry);
    }
}
