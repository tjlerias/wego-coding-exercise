package com.tj.wegocodingexercise.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "DataGovSGClient", url = "${data.gov.sg.client.url}")
public interface DataGovSGClient {

    /**
     * Gets the latest carpark availability in Singapore
     *
     * @return The latest carpark availability
     */
    @GetMapping("/v1/transport/carpark-availability")
    CarparkAvailabilityResponse getCarparkAvailability();
}
