package com.tj.wegocodingexercise.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CarParkInfo(
    @JsonProperty("total_lots") int totalLots,
    @JsonProperty("lot_type") String lotType,
    @JsonProperty("lots_available") int availableLots
) {
}
