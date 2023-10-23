package com.tj.wegocodingexercise.service;

import com.tj.wegocodingexercise.client.CarparkAvailabilityResponse;
import com.tj.wegocodingexercise.client.CarparkData;
import com.tj.wegocodingexercise.client.CarparkInfo;
import com.tj.wegocodingexercise.client.DataGovSGClient;
import com.tj.wegocodingexercise.client.Item;
import com.tj.wegocodingexercise.dto.CarparkAvailabilityDTO;
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

    public Map<String, CarparkAvailabilityDTO> getCarparkAvailability() {
        CarparkAvailabilityResponse response = dataGovSGClient.getCarparkAvailability();

        return response.items().stream()
            .map(Item::carparkData)
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(
                CarparkData::carparkNumber,
                this::buildFrom,
                // Multiple CarparkData entries can have the same carpark number but different
                // lot types in their carpark info, thus we need to merge their details.
                this::mergeCarparkAvailability
            ));
    }

    private CarparkAvailabilityDTO mergeCarparkAvailability(
        CarparkAvailabilityDTO existingCarparkAvailability,
        CarparkAvailabilityDTO newCarparkAvailability
    ) {
        LocalDateTime lastUpdated = existingCarparkAvailability.lastUpdated().isAfter(newCarparkAvailability.lastUpdated())
            ? existingCarparkAvailability.lastUpdated()
            : newCarparkAvailability.lastUpdated();

        return new CarparkAvailabilityDTO(
            existingCarparkAvailability.carparkNumber(),
            existingCarparkAvailability.totalLots() + newCarparkAvailability.totalLots(),
            existingCarparkAvailability.availableLots() + newCarparkAvailability.availableLots(),
            lastUpdated
        );
    }

    private CarparkAvailabilityDTO buildFrom(CarparkData carparkData) {
        int totalLots = carparkData.carparkInfo().stream()
            .mapToInt(CarparkInfo::totalLots)
            .sum();

        int availableLots = carparkData.carparkInfo().stream()
            .mapToInt(CarparkInfo::availableLots)
            .sum();

        return new CarparkAvailabilityDTO(
            carparkData.carparkNumber(),
            totalLots,
            availableLots,
            carparkData.lastUpdated()
        );
    }
}
