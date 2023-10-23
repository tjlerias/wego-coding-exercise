package com.tj.wegocodingexercise.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CarparkDetails(
    String address,
    Double latitude,
    Double longitude,
    @JsonProperty("total_lots") Integer totalLots,
    @JsonProperty("available_lots") Integer availableLots
) {
}
