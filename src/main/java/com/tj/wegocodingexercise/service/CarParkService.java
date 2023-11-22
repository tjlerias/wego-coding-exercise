package com.tj.wegocodingexercise.service;

import com.tj.wegocodingexercise.config.CacheConfig;
import com.tj.wegocodingexercise.dto.CarParkAvailabilityDTO;
import com.tj.wegocodingexercise.dto.CarParkDetails;
import com.tj.wegocodingexercise.dto.NearestCarParksRequest;
import com.tj.wegocodingexercise.entity.CarPark;
import com.tj.wegocodingexercise.repository.CarParkRepository;
import com.tj.wegocodingexercise.util.CoordinateTransformUtil;
import com.tj.wegocodingexercise.util.ResourceProvider;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

import static com.tj.wegocodingexercise.config.CacheConfig.CAR_PARK_AVAILABILITY_SECONDARY_KEY;

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
    private final RedisTemplate<String, Object> redisTemplate;

    public CarParkService(
        CarParkRepository carParkRepository,
        DataGovSGService dataGovSGService,
        ResourceProvider resourceProvider,
        @Value("${car.park.information.csv.path}") String carParkInformationCsvPath,
        RedisTemplate<String, Object> redisTemplate
    ) {
        this.carParkRepository = carParkRepository;
        this.dataGovSGService = dataGovSGService;
        this.resourceProvider = resourceProvider;
        this.carParkInformationCsvPath = carParkInformationCsvPath;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public void loadCarParkData() {
        if (carParkRepository.count() > 0) {
            // No need to load data again
            return;
        }

        CompletableFuture<Map<String, CarParkAvailabilityDTO>> carParkAvailabilityFuture =
            CompletableFuture.supplyAsync(dataGovSGService::getCarParkAvailability);

        CompletableFuture<List<CarPark>> carParkFuture =
            CompletableFuture.supplyAsync(() -> loadFromCSVResource(carParkInformationCsvPath));

        carParkAvailabilityFuture.thenCombine(carParkFuture, this::getCarParksWithAvailabilityInformation)
            .thenAccept(carParkRepository::saveAll)
            .thenRun(() -> logger.info("Loading car park data to the database success."));
    }

    private List<CarPark> getCarParksWithAvailabilityInformation(
        Map<String, CarParkAvailabilityDTO> carParkAvailability,
        List<CarPark> carParks
    ) {
        return carParks.stream()
            .filter(c -> carParkAvailability.get(c.getId()) != null)
            .toList();
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
                    Precision.round(coordinate.x, 5),
                    Precision.round(coordinate.y, 5)
                ))
        );
    }

    @Transactional
    public List<CarParkDetails> getNearestCarParks(NearestCarParksRequest request, Pageable pageable) {
        Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        Page<CarPark> carParks = carParkRepository.findNearestCarParks(location, request.distance(), pageable);

        Map<String, CarParkAvailabilityDTO> availabilityPerCarPark = getCarParkAvailability();

        if (availabilityPerCarPark == null) {
            return new ArrayList<>();
        }

        return carParks.getContent().stream()
            .map(c -> buildFrom(c, availabilityPerCarPark))
            .toList();
    }

    private Map<String, CarParkAvailabilityDTO> getCarParkAvailability() {
        Map<String, CarParkAvailabilityDTO> availabilityPerCarPark =
            (Map<String, CarParkAvailabilityDTO>)
                redisTemplate.opsForValue().get(CacheConfig.CAR_PARK_AVAILABILITY_PRIMARY_KEY);

        if (availabilityPerCarPark == null) {
            return (Map<String, CarParkAvailabilityDTO>)
                redisTemplate.opsForValue().get(CAR_PARK_AVAILABILITY_SECONDARY_KEY);
        }

        return availabilityPerCarPark;
    }

    private CarParkDetails buildFrom(CarPark carPark, Map<String, CarParkAvailabilityDTO> availabilityPerCarPark) {
        Integer totalLots = availabilityPerCarPark.get(carPark.getId()).totalLots();
        Integer availableLots = availabilityPerCarPark.get(carPark.getId()).availableLots();

        return new CarParkDetails(
            carPark.getAddress(),
            carPark.getLocation().getY(),
            carPark.getLocation().getX(),
            totalLots,
            availableLots
        );
    }
}
