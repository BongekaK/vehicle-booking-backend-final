package com.vehiclebooking.backend.config;

import com.vehiclebooking.backend.dto.VehicleDto;
import com.vehiclebooking.backend.entity.Vehicle;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vehiclebooking.backend.repository.VehicleRepository;
import com.vehiclebooking.backend.dto.BookingResponseDto;
import com.vehiclebooking.backend.dto.BookingManageDto;
import com.vehiclebooking.backend.dto.BookingRequestDto;
import com.vehiclebooking.backend.dto.PassengerDto;
import com.vehiclebooking.backend.entity.Booking;
import com.vehiclebooking.backend.entity.Passenger;

@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper(VehicleRepository vehicleRepository) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // Vehicle to VehicleDto mapping
        modelMapper.createTypeMap(Vehicle.class, VehicleDto.class)
                .addMapping(Vehicle::getId, VehicleDto::setId)
                .addMapping(Vehicle::getId, VehicleDto::setPlate);

        // VehicleDto to Vehicle mapping
        modelMapper.createTypeMap(VehicleDto.class, Vehicle.class)
                .addMapping(VehicleDto::getId, Vehicle::setId);

        // BookingRequestDto to Booking mapping
        modelMapper.createTypeMap(BookingRequestDto.class, Booking.class);

        // Booking to BookingResponseDto mapping
        modelMapper.createTypeMap(Booking.class, BookingResponseDto.class)
                .addMapping(src -> src.getUser().getId(), BookingResponseDto::setUserId)
                .addMapping(src -> src.getAllocatedVehicle(), BookingResponseDto::setAllocatedVehicle)
                .addMapping(src -> src.getPassengers(), BookingResponseDto::setPassengers);

        // Booking to BookingManageDto mapping
        modelMapper.createTypeMap(Booking.class, BookingManageDto.class)
                .addMapping(src -> src.getUser().getId(), BookingManageDto::setUserId);

        // Passenger to PassengerDto mapping
        modelMapper.createTypeMap(Passenger.class, PassengerDto.class);

        return modelMapper;
    }

}
