package com.vehiclebooking.backend.service;

public class VehicleAlreadyBookedException extends RuntimeException {
    public VehicleAlreadyBookedException(String message) {
        super(message);
    }
}
