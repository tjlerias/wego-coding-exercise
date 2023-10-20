package com.tj.wegocodingexercise.dto;

import java.time.LocalDateTime;

public record CarparkAvailabilityDTO(
    String carparkNumber,
    int totalLots,
    int availableLots,
    LocalDateTime lastUpdated
) {
}
