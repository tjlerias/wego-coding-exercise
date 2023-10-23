package com.tj.wegocodingexercise.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "DataGovSGClient", url = "${data.gov.sg.client.url}")
public interface DataGovSGClient {

    /**
     * Gets the latest car park availability in Singapore
     *
     * @return The latest car park availability
     */
    @GetMapping("/v1/transport/carpark-availability")
    CarParkAvailabilityResponse getCarParkAvailability();
}
