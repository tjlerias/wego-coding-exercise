package com.tj.wegocodingexercise.service;

import com.tj.wegocodingexercise.dto.CarParkAvailabilityDTO;
import com.tj.wegocodingexercise.dto.CarParkDetails;
import com.tj.wegocodingexercise.dto.NearestCarParksRequest;
import com.tj.wegocodingexercise.entity.CarPark;
import com.tj.wegocodingexercise.repository.CarParkRepository;
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
class CarParkServiceTest {

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    private static final String CAR_PARK_INFORMATION_CSV_PATH = "/test/test.csv";
    private static final String MOCKED_CSV_DATA = """
        car_park_no,address,x_coord,y_coord,car_park_type,type_of_parking_system,short_term_parking,free_parking,night_parking,car_park_decks,gantry_height,car_park_basement
        ACB,BLK 270/271 ALBERT CENTRE BASEMENT CAR PARK,30314.7936,31490.4942,BASEMENT CAR PARK,ELECTRONIC PARKING,WHOLE DAY,NO,YES,1,1.8,Y
        ACM,BLK 98A ALJUNIED CRESCENT,33758.4143,33695.5198,MULTI-STOREY CAR PARK,ELECTRONIC PARKING,WHOLE DAY,SUN & PH FR 7AM-10.30PM,YES,5,2.1,N
        """;
    private static final Map<String, CarParkAvailabilityDTO> availabilityPerCarPark = Map.of(
        "ACB", new CarParkAvailabilityDTO("ACB", 100, 50, LocalDateTime.now()),
        "ACM", new CarParkAvailabilityDTO("ACM", 123, 12, LocalDateTime.now())
    );

    private static final List<CarPark> CAR_PARKS = List.of(
        new CarPark(
            "ACB",
            "BLK 270/271 ALBERT CENTRE BASEMENT CAR PARK",
            geometryFactory.createPoint(new Coordinate(103.85412, 1.30106)),
            100,
            50,
            LocalDateTime.now().plusDays(1)),
        new CarPark("ACM",
            "BLK 98A ALJUNIED CRESCENT",
            geometryFactory.createPoint(new Coordinate(103.88506, 1.321)),
            123,
            12,
            LocalDateTime.now().plusDays(1))
    );

    private static final List<CarPark> CAR_PARKS_WITH_OUTDATED_AVAILABILITY = List.of(
        new CarPark(
            "ACB",
            "BLK 270/271 ALBERT CENTRE BASEMENT CAR PARK",
            geometryFactory.createPoint(new Coordinate(103.85412, 1.30106)),
            25,
            10,
            LocalDateTime.now().minusDays(1)),
        new CarPark("ACM",
            "BLK 98A ALJUNIED CRESCENT",
            geometryFactory.createPoint(new Coordinate(103.88506, 1.321)),
            500,
            20,
            LocalDateTime.now().minusDays(1))
    );


    @Mock
    private CarParkRepository carParkRepository;

    @Mock
    private DataGovSGService dataGovSGService;

    @Mock
    private ResourceProvider resourceProvider;

    @Captor
    private ArgumentCaptor<List<CarPark>> carParkListCaptor;

    private CarParkService carParkService;

    private static List<CarParkDetails> getCarParkDetails(List<CarPark> carParks) {
        CarPark carPark1 = carParks.get(0);
        CarPark carPark2 = carParks.get(1);
        return List.of(
            new CarParkDetails(
                carPark1.getAddress(),
                carPark1.getLocation().getY(),
                carPark1.getLocation().getX(),
                carPark1.getAvailability().getTotalLots(),
                carPark1.getAvailability().getAvailableLots()
            ),
            new CarParkDetails(
                carPark2.getAddress(),
                carPark2.getLocation().getY(),
                carPark2.getLocation().getX(),
                carPark2.getAvailability().getTotalLots(),
                carPark2.getAvailability().getAvailableLots()
            )
        );
    }

    @BeforeEach
    void setUp() {
        carParkService = new CarParkService(carParkRepository, dataGovSGService,
            resourceProvider, CAR_PARK_INFORMATION_CSV_PATH);
    }

    @Test
    void loadCarParkData_success() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(MOCKED_CSV_DATA.getBytes());

        when(carParkRepository.count()).thenReturn(0L);
        when(resourceProvider.getInputStream(CAR_PARK_INFORMATION_CSV_PATH)).thenReturn(inputStream);
        when(dataGovSGService.getCarParkAvailability()).thenReturn(availabilityPerCarPark);

        carParkService.loadCarparkData();

        verify(carParkRepository).count();
        verify(resourceProvider).getInputStream(CAR_PARK_INFORMATION_CSV_PATH);
        verify(dataGovSGService).getCarParkAvailability();
        verify(carParkRepository).saveAll(carParkListCaptor.capture());
        List<CarPark> saveAllCarParkArgument = carParkListCaptor.getValue();
        assertCarParkEquals(saveAllCarParkArgument.get(0), CAR_PARKS.get(0));
        assertCarParkEquals(saveAllCarParkArgument.get(1), CAR_PARKS.get(1));
        verifyNoMoreInteractions(resourceProvider, dataGovSGService, carParkRepository);
    }

    @Test
    void loadCarParkData_loaded_doNothing() {
        when(carParkRepository.count()).thenReturn(2284L);

        carParkService.loadCarparkData();

        verify(carParkRepository).count();
        verifyNoMoreInteractions(resourceProvider, dataGovSGService, carParkRepository);
    }

    @Test
    void loadCarParkData_error_throwRuntimeException() throws IOException {
        when(carParkRepository.count()).thenReturn(0L);
        when(resourceProvider.getInputStream(CAR_PARK_INFORMATION_CSV_PATH)).thenThrow(new FileNotFoundException("File not found"));

        assertThatThrownBy(() -> carParkService.loadCarparkData())
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error loading car parks from CSV");

        verify(carParkRepository).count();
        verify(resourceProvider).getInputStream(CAR_PARK_INFORMATION_CSV_PATH);
        verifyNoMoreInteractions(resourceProvider, dataGovSGService, carParkRepository);
    }

    @Test
    void getNearestCarParks_success() {
        NearestCarParksRequest request = new NearestCarParksRequest(1.37326, 103.897, 500);
        Pageable pageable = PageRequest.of(0, 10);
        Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        Page<CarPark> pagedCarparks = new PageImpl<>(CAR_PARKS, pageable, 2);
        List<CarParkDetails> expected = getCarParkDetails(CAR_PARKS);

        when(carParkRepository.findNearestCarParks(location, request.distance(), pageable))
            .thenReturn(pagedCarparks);
        when(dataGovSGService.getCarParkAvailability()).thenReturn(availabilityPerCarPark);

        List<CarParkDetails> actual = carParkService.getNearestCarParks(request, pageable);

        assertThat(actual).isEqualTo(expected);

        verify(carParkRepository).findNearestCarParks(location, request.distance(), pageable);
        verify(dataGovSGService).getCarParkAvailability();
        verify(carParkRepository).findNearestCarParks(location, request.distance(), pageable);
        verifyNoMoreInteractions(carParkRepository, dataGovSGService);
    }

    @Test
    void getNearestCarParks_withOutdatedAvailability_success() {
        NearestCarParksRequest request = new NearestCarParksRequest(1.37326, 103.897, 500);
        Pageable pageable = PageRequest.of(0, 10);
        Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        Page<CarPark> pagedCarParks = new PageImpl<>(CAR_PARKS_WITH_OUTDATED_AVAILABILITY, pageable, 2);
        List<CarParkDetails> expected = getCarParkDetails(CAR_PARKS);

        when(carParkRepository.findNearestCarParks(location, request.distance(), pageable))
            .thenReturn(pagedCarParks);
        when(dataGovSGService.getCarParkAvailability()).thenReturn(availabilityPerCarPark);

        List<CarParkDetails> actual = carParkService.getNearestCarParks(request, pageable);

        assertThat(actual).isEqualTo(expected);

        verify(carParkRepository).findNearestCarParks(location, request.distance(), pageable);
        verify(dataGovSGService).getCarParkAvailability();
        verify(carParkRepository).saveAll(carParkListCaptor.capture());
        List<CarPark> saveAllCarParkArgument = carParkListCaptor.getValue();
        assertCarParkEquals(saveAllCarParkArgument.get(0), CAR_PARKS.get(0));
        assertCarParkEquals(saveAllCarParkArgument.get(1), CAR_PARKS.get(1));
        verifyNoMoreInteractions(carParkRepository, dataGovSGService);
    }

    private void assertCarParkEquals(CarPark actual, CarPark expected) {
        assertThat(actual)
            .isNotNull()
            .extracting(
                CarPark::getId,
                CarPark::getAddress,
                CarPark::getLocation,
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