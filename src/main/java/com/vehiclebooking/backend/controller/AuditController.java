package com.vehiclebooking.backend.controller;

import com.vehiclebooking.backend.dto.AuditLogDto;
import com.vehiclebooking.backend.entity.AuditEntry; // Added import
import com.vehiclebooking.backend.repository.AuditRepository; // Added import
import com.vehiclebooking.backend.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security/audit")
public class AuditController {

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private AuditService auditService;

    @GetMapping("/logs")
    @PreAuthorize("hasAnyAuthority('SECURITY', 'ADMIN')")
    public List<AuditEntry> getAuditLogs() {
        return auditRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    @PostMapping("/log")
    public void logFrontendAction(@Valid @RequestBody AuditLogDto auditLogDto) {
        auditService.logAction(auditLogDto);
    }
}
