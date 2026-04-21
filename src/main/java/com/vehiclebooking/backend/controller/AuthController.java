package com.vehiclebooking.backend.controller;

import com.vehiclebooking.backend.dto.LoginRequestDto;
import com.vehiclebooking.backend.dto.LoginResponseDto;
import com.vehiclebooking.backend.dto.UserDto;
import com.vehiclebooking.backend.service.UserService;
import com.vehiclebooking.backend.repository.UserRepository;
import com.vehiclebooking.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder; // Added import
import com.vehiclebooking.backend.security.JwtUtils; // Added import
import org.springframework.security.core.userdetails.UserDetailsService; // Added import

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder; // Added field
    private final JwtUtils jwtUtils; // Added field
    private final UserDetailsService userDetailsService; // Added field

    @GetMapping("/generate-hash")
    public String generateHash(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }

    @PostMapping("/bulk-register")
    public List<UserDto> bulkRegister(@RequestBody List<UserDto> users) {
        return userService.bulkCreateUsers(users);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        String trimmedEmail = loginRequestDto.getEmail() != null ? loginRequestDto.getEmail().trim() : null;
        String trimmedPassword = loginRequestDto.getPassword() != null ? loginRequestDto.getPassword().trim() : null;

        System.out.println("Login attempt for email: " + trimmedEmail);
        
        userRepository.findByEmail(trimmedEmail).ifPresent(user -> {
            boolean matches = passwordEncoder.matches(trimmedPassword, user.getPassword());
            System.out.println("Manual Match Check: " + matches);
            System.out.println("Stored Hash from DB: " + user.getPassword());
        });

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(trimmedEmail, trimmedPassword)
        );

        User user = userRepository.findByEmail(trimmedEmail)
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        if (!user.isActive()) {
            System.out.println("User account is locked: " + user.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is locked.");
        }

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtils.generateToken(userDetails);

        UserDto userDto = modelMapper.map(user, UserDto.class);
        
        return ResponseEntity.ok(new LoginResponseDto(token, userDto));
    }
}
