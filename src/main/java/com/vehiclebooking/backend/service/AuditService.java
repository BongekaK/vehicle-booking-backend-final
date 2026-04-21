package com.vehiclebooking.backend.service;

import com.vehiclebooking.backend.dto.AuditLogDto;

public interface AuditService {
    void logAction(String action, String details);
    void logAction(AuditLogDto auditLogDto);
}
