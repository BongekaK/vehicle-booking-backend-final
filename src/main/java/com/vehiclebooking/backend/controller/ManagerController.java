package com.vehiclebooking.backend.controller;

import com.vehiclebooking.backend.dto.ManagerOptionDto;
import com.vehiclebooking.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/managers")
@RequiredArgsConstructor
public class ManagerController {

    private final UserService userService;

    @GetMapping
    public List<ManagerOptionDto> getAllManagers() {
        return userService.getAllManagers();
    }
}
