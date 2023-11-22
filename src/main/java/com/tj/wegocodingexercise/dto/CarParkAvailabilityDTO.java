package com.tj.wegocodingexercise.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record CarParkAvailabilityDTO(
    String carParkNumber,
    int totalLots,
    int availableLots,
    LocalDateTime lastUpdated
) implements Serializable {
}
