package com.tj.wegocodingexercise.service;

import com.tj.wegocodingexercise.dto.CarparkAvailabilityDTO;
import com.tj.wegocodingexercise.dto.CarparkDetails;
import com.tj.wegocodingexercise.dto.NearestCarparksRequest;
import com.tj.wegocodingexercise.entity.Carpark;
import com.tj.wegocodingexercise.repository.CarparkRepository;
import com.tj.wegocodingexercise.util.ResourceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarparkServiceTest {

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    private static final String CARPARK_INFORMATION_CSV_PATH = "/test/test.csv";
    private static final String MOCKED_CSV_DATA = """
        car_park_no,address,x_coord,y_coord,car_park_type,type_of_parking_system,short_term_parking,free_parking,night_parking,car_park_decks,gantry_height,car_park_basement
        ACB,BLK 270/271 ALBERT CENTRE BASEMENT CAR PARK,30314.7936,31490.4942,BASEMENT CAR PARK,ELECTRONIC PARKING,WHOLE DAY,NO,YES,1,1.8,Y
        ACM,BLK 98A ALJUNIED CRESCENT,33758.4143,33695.5198,MULTI-STOREY CAR PARK,ELECTRONIC PARKING,WHOLE DAY,SUN & PH FR 7AM-10.30PM,YES,5,2.1,N
        """;
    private static final Map<String, CarparkAvailabilityDTO> availabilityPerCarpark = Map.of(
        "ACB", new CarparkAvailabilityDTO("ACB", 100, 50, LocalDateTime.now()),
        "ACM", new CarparkAvailabilityDTO("ACM", 123, 12, LocalDateTime.now())
    );

    private static final List<Carpark> carparks = List.of(
        new Carpark(
            "ACB",
            "BLK 270/271 ALBERT CENTRE BASEMENT CAR PARK",
            geometryFactory.createPoint(new Coordinate(103.8541, 1.3011)),
            100,
            50,
            LocalDateTime.now().plusDays(1)),
        new Carpark("ACM",
            "BLK 98A ALJUNIED CRESCENT",
            geometryFactory.createPoint(new Coordinate(103.8851, 1.321)),
            123,
            12,
            LocalDateTime.now().plusDays(1))
    );

    private static final List<Carpark> carparksWithOutdatedAvailability = List.of(
        new Carpark(
            "ACB",
            "BLK 270/271 ALBERT CENTRE BASEMENT CAR PARK",
            geometryFactory.createPoint(new Coordinate(103.8541, 1.3011)),
            25,
            10,
            LocalDateTime.now().minusDays(1)),
        new Carpark("ACM",
            "BLK 98A ALJUNIED CRESCENT",
            geometryFactory.createPoint(new Coordinate(103.8851, 1.321)),
            500,
            20,
            LocalDateTime.now().minusDays(1))
    );


    @Mock
    private CarparkRepository carparkRepository;

    @Mock
    private DataGovSGService dataGovSGService;

    @Mock
    private ResourceProvider resourceProvider;

    @Captor
    private ArgumentCaptor<List<Carpark>> carparkListCaptor;

    private CarparkService carparkService;

    @BeforeEach
    void setUp() {
        carparkService = new CarparkService(carparkRepository, dataGovSGService,
            resourceProvider, CARPARK_INFORMATION_CSV_PATH);
    }

    @Test
    void loadCarparkData_success() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(MOCKED_CSV_DATA.getBytes());

        when(carparkRepository.count()).thenReturn(0L);
        when(resourceProvider.getInputStream(CARPARK_INFORMATION_CSV_PATH)).thenReturn(inputStream);
        when(dataGovSGService.getCarparkAvailability()).thenReturn(availabilityPerCarpark);

        carparkService.loadCarparkData();

        verify(carparkRepository).count();
        verify(resourceProvider).getInputStream(CARPARK_INFORMATION_CSV_PATH);
        verify(dataGovSGService).getCarparkAvailability();
        verify(carparkRepository).saveAll(carparkListCaptor.capture());
        List<Carpark> saveAllCarparkArgument = carparkListCaptor.getValue();
        assertCarparkEquals(saveAllCarparkArgument.get(0), carparks.get(0));
        assertCarparkEquals(saveAllCarparkArgument.get(1), carparks.get(1));
        verifyNoMoreInteractions(resourceProvider, dataGovSGService, carparkRepository);
    }

    @Test
    void loadCarparkData_loaded_doNothing() {
        when(carparkRepository.count()).thenReturn(2284L);

        carparkService.loadCarparkData();

        verify(carparkRepository).count();
        verifyNoMoreInteractions(resourceProvider, dataGovSGService, carparkRepository);
    }

    @Test
    void loadCarparkData_error_throwRuntimeException() throws IOException {
        when(carparkRepository.count()).thenReturn(0L);
        when(resourceProvider.getInputStream(CARPARK_INFORMATION_CSV_PATH)).thenThrow(new FileNotFoundException("File not found"));

        assertThatThrownBy(() -> carparkService.loadCarparkData())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error loading carparks from CSV");

        verify(carparkRepository).count();
        verify(resourceProvider).getInputStream(CARPARK_INFORMATION_CSV_PATH);
        verifyNoMoreInteractions(resourceProvider, dataGovSGService, carparkRepository);
    }

    @Test
    void getNearestCarParks_success() {
        NearestCarparksRequest request = new NearestCarparksRequest(1.37326, 103.897, 500);
        Pageable pageable = PageRequest.of(0, 10);
        Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        Page<Carpark> pagedCarparks = new PageImpl<>(carparks, pageable, 2);
        List<CarparkDetails> expected = getCarparkDetails(carparks);

        when(carparkRepository.findNearestCarparks(location, request.distance(), pageable))
            .thenReturn(pagedCarparks);
        when(dataGovSGService.getCarparkAvailability()).thenReturn(availabilityPerCarpark);

        List<CarparkDetails> actual = carparkService.getNearestCarParks(request, pageable);

        assertThat(actual).isEqualTo(expected);

        verify(carparkRepository).findNearestCarparks(location, request.distance(), pageable);
        verify(dataGovSGService).getCarparkAvailability();
        verify(carparkRepository).findNearestCarparks(location, request.distance(), pageable);
        verifyNoMoreInteractions(carparkRepository, dataGovSGService);
    }

    @Test
    void getNearestCarParks_withOutdatedAvailability_success() {
        NearestCarparksRequest request = new NearestCarparksRequest(1.37326, 103.897, 500);
        Pageable pageable = PageRequest.of(0, 10);
        Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        Page<Carpark> pagedCarparks = new PageImpl<>(carparksWithOutdatedAvailability, pageable, 2);
        List<CarparkDetails> expected = getCarparkDetails(carparks);

        when(carparkRepository.findNearestCarparks(location, request.distance(), pageable))
            .thenReturn(pagedCarparks);
        when(dataGovSGService.getCarparkAvailability()).thenReturn(availabilityPerCarpark);

        List<CarparkDetails> actual = carparkService.getNearestCarParks(request, pageable);

        assertThat(actual).isEqualTo(expected);

        verify(carparkRepository).findNearestCarparks(location, request.distance(), pageable);
        verify(dataGovSGService).getCarparkAvailability();
        verify(carparkRepository).saveAll(carparkListCaptor.capture());
        List<Carpark> saveAllCarparkArgument = carparkListCaptor.getValue();
        assertCarparkEquals(saveAllCarparkArgument.get(0), carparks.get(0));
        assertCarparkEquals(saveAllCarparkArgument.get(1), carparks.get(1));
        verifyNoMoreInteractions(carparkRepository, dataGovSGService);
    }

    private static List<CarparkDetails> getCarparkDetails(List<Carpark> carparks) {
        Carpark carpark1 = carparks.get(0);
        Carpark carpark2 = carparks.get(1);
        return List.of(
            new CarparkDetails(
                carpark1.getAddress(),
                carpark1.getLocation().getY(),
                carpark1.getLocation().getX(),
                carpark1.getAvailability().getTotalLots(),
                carpark1.getAvailability().getAvailableLots()
            ),
            new CarparkDetails(
                carpark2.getAddress(),
                carpark2.getLocation().getY(),
                carpark2.getLocation().getX(),
                carpark2.getAvailability().getTotalLots(),
                carpark2.getAvailability().getAvailableLots()
            )
        );
    }

    private void assertCarparkEquals(Carpark actual, Carpark expected) {
        assertThat(actual)
            .isNotNull()
            .extracting(
                Carpark::getId,
                Carpark::getAddress,
                Carpark::getLocation,
                c -> c.getAvailability().getId(),
                c -> c.getAvailability().getTotalLots(),
                c -> c.getAvailability().getAvailableLots())
            .containsExactly(
                expected.getId(),
                expected.getAddress(),
                expected.getLocation(),
                expected.getAvailability().getId(),
                expected.getAvailability().getTotalLots(),
                expected.getAvailability().getAvailableLots());
    }
}