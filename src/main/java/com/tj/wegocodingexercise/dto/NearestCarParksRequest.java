package com.tj.wegocodingexercise.dto;

import jakarta.validation.constraints.NotNull;

public record NearestCarParksRequest(
    @NotNull(message = "Latitude is required") Double latitude,
    @NotNull(message = "Longitude is required") Double longitude,
    Integer distance
) {
    public NearestCarParksRequest {
        if (distance == null) {
            distance = 500;
        }
    }
}
