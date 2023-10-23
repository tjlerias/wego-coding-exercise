package com.tj.wegocodingexercise.service;

import com.tj.wegocodingexercise.client.CarParkAvailabilityResponse;
import com.tj.wegocodingexercise.client.CarParkData;
import com.tj.wegocodingexercise.client.CarParkInfo;
import com.tj.wegocodingexercise.client.DataGovSGClient;
import com.tj.wegocodingexercise.client.Item;
import com.tj.wegocodingexercise.dto.CarParkAvailabilityDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataGovSGService {

    private final DataGovSGClient dataGovSGClient;

    public DataGovSGService(DataGovSGClient dataGovSGClient) {
        this.dataGovSGClient = dataGovSGClient;
    }

    @Cacheable(cacheNames = "carParkAvailability", sync = true)
    public Map<String, CarParkAvailabilityDTO> getCarParkAvailability() {
        CarParkAvailabilityResponse response = dataGovSGClient.getCarParkAvailability();

        return response.items().stream()
            .map(Item::carParkData)
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(
                CarParkData::carParkNumber,
                this::buildFrom,
                // Multiple CarParkData entries can have the same car park number but different
                // lot types in their car park info, thus we need to merge their details.
                this::mergeCarParkAvailability
            ));
    }

    private CarParkAvailabilityDTO mergeCarParkAvailability(
        CarParkAvailabilityDTO existingCarParkAvailability,
        CarParkAvailabilityDTO newCarParkAvailability
    ) {
        LocalDateTime lastUpdated = existingCarParkAvailability.lastUpdated().isAfter(newCarParkAvailability.lastUpdated())
            ? existingCarParkAvailability.lastUpdated()
            : newCarParkAvailability.lastUpdated();

        return new CarParkAvailabilityDTO(
            existingCarParkAvailability.carParkNumber(),
            existingCarParkAvailability.totalLots() + newCarParkAvailability.totalLots(),
            existingCarParkAvailability.availableLots() + newCarParkAvailability.availableLots(),
            lastUpdated
        );
    }

    private CarParkAvailabilityDTO buildFrom(CarParkData carParkData) {
        int totalLots = carParkData.carParkInfo().stream()
            .mapToInt(CarParkInfo::totalLots)
            .sum();

        int availableLots = carParkData.carParkInfo().stream()
            .mapToInt(CarParkInfo::availableLots)
            .sum();

        return new CarParkAvailabilityDTO(
            carParkData.carParkNumber(),
            totalLots,
            availableLots,
            carParkData.lastUpdated()
        );
    }
}
