package com.tj.wegocodingexercise.service;

import com.tj.wegocodingexercise.dto.CarParkAvailabilityDTO;
import com.tj.wegocodingexercise.dto.CarParkDetails;
import com.tj.wegocodingexercise.dto.NearestCarParksRequest;
import com.tj.wegocodingexercise.entity.CarPark;
import com.tj.wegocodingexercise.entity.CarParkAvailability;
import com.tj.wegocodingexercise.repository.CarParkRepository;
import com.tj.wegocodingexercise.util.CoordinateTransformUtil;
import com.tj.wegocodingexercise.util.ResourceProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.util.Precision;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.proj4j.ProjCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

@Service
@Transactional(readOnly = true)
public class CarParkService {

    private static final Logger logger = LoggerFactory.getLogger(CarParkService.class);

    private static final String CAR_PARK_NUMBER = "car_park_no";
    private static final String ADDRESS = "address";
    private static final String X_COORDINATE = "x_coord";
    private static final String Y_COORDINATE = "y_coord";
    private static final String CAR_PARK_TYPE = "car_park_type";
    private static final String PARKING_SYSTEM_TYPE = "type_of_parking_system";
    private static final String SHORT_TERM_PARKING = "short_term_parking";
    private static final String FREE_PARKING = "free_parking";
    private static final String NIGHT_PARKING = "night_parking";
    private static final String CAR_PARK_DECKS = "car_park_decks";
    private static final String GANTRY_HEIGHT = "gantry_height";
    private static final String CAR_PARK_BASEMENT = "car_park_basement";
    private static final String[] CAR_PARK_CSV_HEADERS = {CAR_PARK_NUMBER, ADDRESS, X_COORDINATE, Y_COORDINATE,
        CAR_PARK_TYPE, PARKING_SYSTEM_TYPE, SHORT_TERM_PARKING, FREE_PARKING, NIGHT_PARKING, CAR_PARK_DECKS,
        GANTRY_HEIGHT, CAR_PARK_BASEMENT};

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    private final CarParkRepository carParkRepository;
    private final DataGovSGService dataGovSGService;
    private final ResourceProvider resourceProvider;
    private final String carParkInformationCsvPath;

    public CarParkService(
        CarParkRepository carParkRepository,
        DataGovSGService dataGovSGService,
        ResourceProvider resourceProvider,
        @Value("${car.park.information.csv.path}") String carParkInformationCsvPath
    ) {
        this.carParkRepository = carParkRepository;
        this.dataGovSGService = dataGovSGService;
        this.resourceProvider = resourceProvider;
        this.carParkInformationCsvPath = carParkInformationCsvPath;
    }

    @Transactional
    public void loadCarparkData() {
        if (carParkRepository.count() > 0) {
            // No need to load data again
            return;
        }

        logger.info("Loading car park data to the database start.");

        List<CarPark> carParks = loadFromCSVResource(carParkInformationCsvPath);

        createCarparkAvailability(carParks);

        carParkRepository.saveAll(carParks);

        logger.info("Loading car park data to the database end.");
    }

    private void createCarparkAvailability(List<CarPark> carParks) {
        Map<String, CarParkAvailabilityDTO> availabilityPerCarpark =
            dataGovSGService.getCarParkAvailability();

        carParks.forEach(carPark ->
            Optional.ofNullable(availabilityPerCarpark.get(carPark.getId()))
                .ifPresent(a -> carPark.setAvailability(
                    new CarParkAvailability(carPark, a.totalLots(), a.availableLots()))
                ));
    }

    private List<CarPark> loadFromCSVResource(String resourcePath) {
        try (InputStream inputStream = resourceProvider.getInputStream(resourcePath)) {
            try (Reader reader = new InputStreamReader(inputStream)) {
                CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(CAR_PARK_CSV_HEADERS)
                    .setSkipHeaderRecord(true)
                    .build();

                Iterable<CSVRecord> records = csvFormat.parse(reader);

                return StreamSupport.stream(records.spliterator(), false)
                    .map(this::buildFrom)
                    .toList();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading car parks from CSV", e);
        }
    }

    private CarPark buildFrom(CSVRecord csvRecord) {
        ProjCoordinate coordinate = CoordinateTransformUtil.transform(
            CoordinateTransformUtil.SVY21,
            CoordinateTransformUtil.WGS84,
            new ProjCoordinate(
                Double.parseDouble(csvRecord.get(X_COORDINATE)),
                Double.parseDouble(csvRecord.get(Y_COORDINATE))
            )
        );

        return new CarPark(
            csvRecord.get(CAR_PARK_NUMBER),
            csvRecord.get(ADDRESS),
            geometryFactory.createPoint(
                new Coordinate(
                    Precision.round(coordinate.x, 4),
                    Precision.round(coordinate.y, 4)
                ))
        );
    }

    @Transactional
    public List<CarParkDetails> getNearestCarParks(NearestCarParksRequest request, Pageable pageable) {
        Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        Page<CarPark> carParks = carParkRepository.findNearestCarParks(location, request.distance(), pageable);

        updateCarParkAvailability(carParks.getContent());

        return carParks.stream()
            .map(this::buildFrom)
            .toList();
    }

    private void updateCarParkAvailability(List<CarPark> carParks) {
        Map<String, CarParkAvailabilityDTO> availabilityPerCarPark =
            dataGovSGService.getCarParkAvailability();

        List<CarPark> carParkWithOutdatedAvailability = carParks.stream()
            .filter(hasOutdatedAvailability(availabilityPerCarPark))
            .toList();

        if (CollectionUtils.isNotEmpty(carParkWithOutdatedAvailability)) {
            carParkWithOutdatedAvailability.forEach(updateCarParkAvailability(availabilityPerCarPark));
            carParkRepository.saveAll(carParkWithOutdatedAvailability);
        }
    }

    private Consumer<CarPark> updateCarParkAvailability(Map<String, CarParkAvailabilityDTO> availabilityPerCarPark) {
        return carParkWithOutdatedAvailability -> {
            CarParkAvailabilityDTO updatedAvailability =
                availabilityPerCarPark.get(carParkWithOutdatedAvailability.getId());
            CarParkAvailability availability = carParkWithOutdatedAvailability.getAvailability();
            availability.setTotalLots(updatedAvailability.totalLots());
            availability.setAvailableLots(updatedAvailability.availableLots());
        };
    }

    private Predicate<CarPark> hasOutdatedAvailability(Map<String, CarParkAvailabilityDTO> availabilityPerCarPark) {
        return carPark -> {
            CarParkAvailabilityDTO updatedAvailability = availabilityPerCarPark.get(carPark.getId());
            return updatedAvailability != null && carPark.getAvailability().getUpdatedAt()
                .isBefore(updatedAvailability.lastUpdated());
        };
    }

    private CarParkDetails buildFrom(CarPark carPark) {
        return new CarParkDetails(
            carPark.getAddress(),
            carPark.getLocation().getY(),
            carPark.getLocation().getX(),
            carPark.getAvailability().getTotalLots(),
            carPark.getAvailability().getAvailableLots()
        );
    }
}
