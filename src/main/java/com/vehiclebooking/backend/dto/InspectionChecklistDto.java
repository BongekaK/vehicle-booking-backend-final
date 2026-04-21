package com.vehiclebooking.backend.dto;

import java.util.List;
import lombok.Data;

@Data
public class InspectionChecklistDto {

    private Long bookingId;
    private Long checkedById;
    private List<InspectionItemDto> items;
}
