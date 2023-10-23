package com.tj.wegocodingexercise.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"address", "latitude", "longitude", "total_lots", "available_lots"})
public record CarParkDetails(
    String address,
    Double latitude,
    Double longitude,
    @JsonProperty("total_lots") Integer totalLots,
    @JsonProperty("available_lots") Integer availableLots
) {
}
