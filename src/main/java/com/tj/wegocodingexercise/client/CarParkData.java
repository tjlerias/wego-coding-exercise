package com.tj.wegocodingexercise.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record CarParkData(
    @JsonProperty("carpark_info") List<CarParkInfo> carParkInfo,
    @JsonProperty("carpark_number") String carParkNumber,
    @JsonProperty("update_datetime") LocalDateTime lastUpdated
) {
}
