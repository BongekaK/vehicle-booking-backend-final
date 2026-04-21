package com.vehiclebooking.backend.controller;

import com.vehiclebooking.backend.dto.InspectionDto;
import com.vehiclebooking.backend.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'FLEET_MANAGER', 'EMPLOYEE', 'SECURITY', 'OPS_OFFICIAL', 'LINE_MANAGER')")
    @PostMapping
    public InspectionDto saveInspection(@RequestBody InspectionDto inspectionDto) {
        return inspectionService.saveInspection(inspectionDto);
    }
}
