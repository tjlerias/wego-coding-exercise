package com.tj.wegocodingexercise.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "DataGovSGClient", url = "${data.gov.sg.client.url}")
public interface DataGovSGClient {

    /**
     * Gets the latest carpark availability in Singapore
     *
     * @param dateTime The date time to retrieve the latest carpark availability
     * @return The latest carpark availability
     */
    @GetMapping("/v1/transport/carpark-availability")
    CarparkAvailabilityResponse getCarparkAvailability(@RequestParam(name = "date_time", required = false) LocalDateTime dateTime);
}
