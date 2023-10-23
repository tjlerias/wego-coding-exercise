package com.tj.wegocodingexercise.service;

import com.tj.wegocodingexercise.client.CarParkAvailabilityResponse;
import com.tj.wegocodingexercise.client.CarParkData;
import com.tj.wegocodingexercise.client.CarParkInfo;
import com.tj.wegocodingexercise.client.DataGovSGClient;
import com.tj.wegocodingexercise.client.Item;
import com.tj.wegocodingexercise.dto.CarParkAvailabilityDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataGovSGServiceTest {

    private static final List<CarParkInfo> CAR_PARK_INFO_1 = List.of(new CarParkInfo(10, "C", 1));
    private static final List<CarParkInfo> CAR_PARK_INFO_2 = List.of(new CarParkInfo(5, "S", 5));
    private static final List<CarParkInfo> CAR_PARK_INFO_3 = List.of(
        new CarParkInfo(10, "C", 5),
        new CarParkInfo(20, "Y", 10)
    );
    private static final List<CarParkData> carParkData = List.of(
        new CarParkData(CAR_PARK_INFO_1, "ACB", LocalDateTime.of(2023, 1, 1, 0, 0)),
        new CarParkData(CAR_PARK_INFO_2, "ACB", LocalDateTime.of(2023, 2, 1, 0, 0)),
        new CarParkData(CAR_PARK_INFO_3, "ACM", LocalDateTime.of(2023, 3, 1, 0, 0))
    );
    private static final List<Item> items = List.of(new Item(ZonedDateTime.now(), carParkData));
    private static final CarParkAvailabilityResponse response = new CarParkAvailabilityResponse(items);
    @Mock
    private DataGovSGClient dataGovSGClient;

    private DataGovSGService dataGovSGService;

    @BeforeEach
    void setUp() {
        dataGovSGService = new DataGovSGService(dataGovSGClient);
    }

    @Test
    void getCarparkAvailability_success() {
        Map<String, CarParkAvailabilityDTO> expected = Map.of(
            "ACB", new CarParkAvailabilityDTO("ACB", 15, 6, LocalDateTime.of(2023, 2, 1, 0, 0)),
            "ACM", new CarParkAvailabilityDTO("ACM", 30, 15, LocalDateTime.of(2023, 3, 1, 0, 0))
        );

        when(dataGovSGClient.getCarParkAvailability()).thenReturn(response);

        Map<String, CarParkAvailabilityDTO> actual = dataGovSGService.getCarParkAvailability();

        assertThat(actual).isEqualTo(expected);

        verify(dataGovSGClient).getCarParkAvailability();
        verifyNoMoreInteractions(dataGovSGClient);
    }
}