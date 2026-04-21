package com.vehiclebooking.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehiclebooking.backend.dto.BookingResponseDto;
import com.vehiclebooking.backend.dto.VehicleDto;
import com.vehiclebooking.backend.service.BookingService;
import com.vehiclebooking.backend.service.UserService;
import com.vehiclebooking.backend.service.InspectionService;
import com.vehiclebooking.backend.dto.InspectionDto;
import com.vehiclebooking.backend.entity.BookingStatus;
import com.vehiclebooking.backend.entity.VehicleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;



    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private InspectionService inspectionService;

    @Test
    @WithMockUser(authorities = "FLEET_MANAGER")
    public void changeAllocatedVehicle_shouldSucceed_whenUserIsFleetManager() throws Exception {
        UUID bookingId = UUID.randomUUID();
        String vehiclePlate = "NEW-PLATE-123";
        BookingResponseDto bookingResponseDto = new BookingResponseDto();
        bookingResponseDto.setId(bookingId);
        VehicleDto vehicleDto = new VehicleDto();
        vehicleDto.setPlate(vehiclePlate);
        bookingResponseDto.setAllocatedVehicle(vehicleDto);

        when(bookingService.changeAllocatedVehicle(eq(bookingId), eq(vehiclePlate))).thenReturn(bookingResponseDto);

        mockMvc.perform(patch("/api/bookings/{id}/change-vehicle", bookingId)
                        .param("vehiclePlate", vehiclePlate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId.toString()))
                .andExpect(jsonPath("$.allocatedVehicle.plate").value(vehiclePlate));
    }

    @Test
    @WithMockUser(authorities = "USER")
    public void changeAllocatedVehicle_shouldFail_whenUserIsNotFleetManager() throws Exception {
        UUID bookingId = UUID.randomUUID();
        String vehiclePlate = "NEW-PLATE-123";

        mockMvc.perform(patch("/api/bookings/{id}/change-vehicle", bookingId)
                        .param("vehiclePlate", vehiclePlate))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "EMPLOYEE")
    public void getInspectionByBookingId_shouldSucceed_whenInspectionExists() throws Exception {
        UUID bookingId = UUID.randomUUID();
        InspectionDto inspectionDto = new InspectionDto();
        inspectionDto.setBookingId(bookingId);

        when(inspectionService.getInspectionByBookingId(eq(bookingId))).thenReturn(inspectionDto);

        mockMvc.perform(get("/api/bookings/{bookingId}/inspection", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(bookingId.toString()));
    }

    @Test
    @WithMockUser(authorities = "EMPLOYEE")
    public void getInspectionByBookingId_shouldFail_whenInspectionDoesNotExist() throws Exception {
        UUID bookingId = UUID.randomUUID();

        when(inspectionService.getInspectionByBookingId(eq(bookingId))).thenReturn(null);

        mockMvc.perform(get("/api/bookings/{bookingId}/inspection", bookingId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "EMPLOYEE")
    public void startTrip_shouldSucceed_whenCalledWithValidBookingId() throws Exception {
        UUID bookingId = UUID.randomUUID();
        BookingResponseDto bookingResponseDto = new BookingResponseDto();
        bookingResponseDto.setId(bookingId);
        bookingResponseDto.setActualStartTime(LocalDateTime.now());
        bookingResponseDto.setStatus(BookingStatus.IN_PROGRESS);

        VehicleDto vehicleDto = new VehicleDto();
        vehicleDto.setStatus(VehicleStatus.IN_TRANSIT);
        bookingResponseDto.setAllocatedVehicle(vehicleDto);

        when(bookingService.startTrip(eq(bookingId))).thenReturn(bookingResponseDto);

        mockMvc.perform(post("/api/bookings/{id}/start-trip", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId.toString()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.allocatedVehicle.status").value("IN_TRANSIT"));
    }

    @Test
    @WithMockUser(authorities = "EMPLOYEE")
    public void startTrip_shouldReturnBadRequest_whenTripAlreadyStarted() throws Exception {
        UUID bookingId = UUID.randomUUID();

        when(bookingService.startTrip(eq(bookingId))).thenThrow(new IllegalStateException("Trip already started"));

        mockMvc.perform(post("/api/bookings/{id}/start-trip", bookingId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "SECURITY")
    public void startTrip_shouldFail_whenUserIsNotAuthorized() throws Exception {
        UUID bookingId = UUID.randomUUID();

        mockMvc.perform(post("/api/bookings/{id}/start-trip", bookingId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "EMPLOYEE")
    public void returnVehicle_shouldSucceed_whenCalledWithValidBookingId() throws Exception {
        UUID bookingId = UUID.randomUUID();
        BookingResponseDto bookingResponseDto = new BookingResponseDto();
        bookingResponseDto.setId(bookingId);
        bookingResponseDto.setActualEndTime(LocalDateTime.now());
        bookingResponseDto.setStatus(BookingStatus.COMPLETED);

        when(bookingService.returnVehicle(eq(bookingId))).thenReturn(bookingResponseDto);

        mockMvc.perform(post("/api/bookings/{id}/return-vehicle", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(authorities = "EMPLOYEE")
    public void returnVehicle_shouldReturnBadRequest_whenTripAlreadyReturned() throws Exception {
        UUID bookingId = UUID.randomUUID();

        when(bookingService.returnVehicle(eq(bookingId))).thenThrow(new IllegalStateException("Trip already returned"));

        mockMvc.perform(post("/api/bookings/{id}/return-vehicle", bookingId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "SECURITY")
    public void returnVehicle_shouldFail_whenUserIsNotAuthorized() throws Exception {
        UUID bookingId = UUID.randomUUID();

        mockMvc.perform(post("/api/bookings/{id}/return-vehicle", bookingId))
                .andExpect(status().isForbidden());
    }
}
