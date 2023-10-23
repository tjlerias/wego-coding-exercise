package com.tj.wegocodingexercise.service;

import com.tj.wegocodingexercise.client.CarparkAvailabilityResponse;
import com.tj.wegocodingexercise.client.CarparkData;
import com.tj.wegocodingexercise.client.CarparkInfo;
import com.tj.wegocodingexercise.client.DataGovSGClient;
import com.tj.wegocodingexercise.client.Item;
import com.tj.wegocodingexercise.dto.CarparkAvailabilityDTO;
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

    private static final List<CarparkInfo> carparkInfo1 = List.of(new CarparkInfo(10, "C", 1));
    private static final List<CarparkInfo> carparkInfo2 = List.of(new CarparkInfo(5, "S", 5));
    private static final List<CarparkInfo> carparkInfo3 = List.of(
        new CarparkInfo(10, "C", 5),
        new CarparkInfo(20, "Y", 10)
    );
    private static final List<CarparkData> carparkData = List.of(
        new CarparkData(carparkInfo1, "ACB", LocalDateTime.of(2023, 1, 1, 0, 0)),
        new CarparkData(carparkInfo2, "ACB", LocalDateTime.of(2023, 2, 1, 0, 0)),
        new CarparkData(carparkInfo3, "ACM", LocalDateTime.of(2023, 3, 1, 0, 0))
    );
    private static final List<Item> items = List.of(new Item(ZonedDateTime.now(), carparkData));
    private static final CarparkAvailabilityResponse response = new CarparkAvailabilityResponse(items);
    @Mock
    private DataGovSGClient dataGovSGClient;

    private DataGovSGService dataGovSGService;

    @BeforeEach
    void setUp() {
        dataGovSGService = new DataGovSGService(dataGovSGClient);
    }

    @Test
    void getCarparkAvailability_success() {
        Map<String, CarparkAvailabilityDTO> expected = Map.of(
            "ACB", new CarparkAvailabilityDTO("ACB", 15, 6, LocalDateTime.of(2023, 2, 1, 0, 0)),
            "ACM", new CarparkAvailabilityDTO("ACM", 30, 15, LocalDateTime.of(2023, 3, 1, 0, 0))
        );

        when(dataGovSGClient.getCarparkAvailability()).thenReturn(response);

        Map<String, CarparkAvailabilityDTO> actual = dataGovSGService.getCarparkAvailability();

        assertThat(actual).isEqualTo(expected);

        verify(dataGovSGClient).getCarparkAvailability();
        verifyNoMoreInteractions(dataGovSGClient);
    }
}