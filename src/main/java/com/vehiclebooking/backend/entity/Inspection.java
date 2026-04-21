package com.vehiclebooking.backend.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import lombok.Data;

import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.UUID;

@Entity
@Table(name = "inspections")
@Data
public class Inspection {

    @Id
    @GeneratedValue
    @JdbcTypeCode(Types.BINARY)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "checked_by_id")
    private User checkedBy;

    @Column(name = "inspection_date")
    private Date inspectionDate;

    @JsonManagedReference
    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<InspectionItem> items;
}
