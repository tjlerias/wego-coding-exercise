package com.tj.wegocodingexercise.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.List;

public record Item(
    ZonedDateTime timestamp,
    @JsonProperty("carpark_data") List<CarparkData> carparkData
) {
}
