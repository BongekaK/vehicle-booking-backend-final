package com.vehiclebooking.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vehicles")
@Data
public class Vehicle {

    @Id
    @Column(name = "plate", unique = true, nullable = false)
    private String id;

    private String make;

    private String model;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VehicleStatus status;

    private Integer currentMileage;
}
