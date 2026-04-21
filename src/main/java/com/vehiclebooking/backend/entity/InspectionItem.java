package com.vehiclebooking.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "inspection_items")
@Data
public class InspectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Column(name = "check_out")
    private String checkOut;

    @Column(name = "check_in")
    private String checkIn;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "inspection_id", nullable = false)
    private Inspection inspection;
}
