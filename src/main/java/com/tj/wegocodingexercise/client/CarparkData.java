package com.tj.wegocodingexercise.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record CarparkData(
    @JsonProperty("carpark_info") List<CarparkInfo> carparkInfo,
    @JsonProperty("carpark_number") String carparkNumber,
    @JsonProperty("update_datetime") LocalDateTime lastUpdated
) {
}
