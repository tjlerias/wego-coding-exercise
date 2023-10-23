package com.tj.wegocodingexercise.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record NearestCarParksRequest(
    @NotNull(message = "Latitude is required") Double latitude,
    @NotNull(message = "Longitude is required") Double longitude,
    @Min(value = 500, message = "Distance must be greater than or equal to 500") Integer distance
) {
    public NearestCarParksRequest {
        if (distance == null) {
            distance = 500;
        }
    }
}
