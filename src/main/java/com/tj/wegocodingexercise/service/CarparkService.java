package com.tj.wegocodingexercise.service;

import com.tj.wegocodingexercise.dto.CarparkAvailabilityDTO;
import com.tj.wegocodingexercise.dto.CarparkDetails;
import com.tj.wegocodingexercise.dto.NearestCarparksRequest;
import com.tj.wegocodingexercise.entity.Carpark;
import com.tj.wegocodingexercise.entity.CarparkAvailability;
import com.tj.wegocodingexercise.repository.CarparkRepository;
import com.tj.wegocodingexercise.util.CoordinateTransformUtil;
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

import java.io.FileNotFoundException;
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
public class CarparkService {

    private static final Logger logger = LoggerFactory.getLogger(CarparkService.class);

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
    private static final String[] CARPARK_CSV_HEADERS = {CAR_PARK_NUMBER, ADDRESS, X_COORDINATE, Y_COORDINATE,
        CAR_PARK_TYPE, PARKING_SYSTEM_TYPE, SHORT_TERM_PARKING, FREE_PARKING, NIGHT_PARKING, CAR_PARK_DECKS,
        GANTRY_HEIGHT, CAR_PARK_BASEMENT};

    private final GeometryFactory geometryFactory = new GeometryFactory();

    private final CarparkRepository carparkRepository;
    private final DataGovSGService dataGovSGService;
    private final String carparkInformationCsvPath;

    public CarparkService(
        CarparkRepository carparkRepository,
        DataGovSGService dataGovSGService,
        @Value("${carpark.information.csv.path}") String carparkInformationCsvPath
    ) {
        this.carparkRepository = carparkRepository;
        this.dataGovSGService = dataGovSGService;
        this.carparkInformationCsvPath = carparkInformationCsvPath;
    }

    @Transactional
    public void loadCarparkData() {
        if (carparkRepository.count() > 0) {
            // No need to load data again
            return;
        }

        logger.info("Loading carpark data to the database start.");

        List<Carpark> carparks = loadFromCSVResource(carparkInformationCsvPath);

        createCarparkAvailability(carparks);

        carparkRepository.saveAll(carparks);

        logger.info("Loading carpark data to the database end.");
    }

    private void createCarparkAvailability(List<Carpark> carparks) {
        Map<String, CarparkAvailabilityDTO> availabilityPerCarpark =
            dataGovSGService.getCarparkAvailability();

        carparks.forEach(carpark ->
            Optional.ofNullable(availabilityPerCarpark.get(carpark.getId()))
                .ifPresent(a -> carpark.setAvailability(
                    new CarparkAvailability(carpark, a.totalLots(), a.availableLots()))
                ));
    }

    private List<Carpark> loadFromCSVResource(String resourcePath) {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("File not found: " + resourcePath);
            }

            try (Reader reader = new InputStreamReader(inputStream)) {
                CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader(CARPARK_CSV_HEADERS)
                    .setSkipHeaderRecord(true)
                    .build();

                Iterable<CSVRecord> records = csvFormat.parse(reader);

                return StreamSupport.stream(records.spliterator(), false)
                    .map(this::buildFrom)
                    .toList();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading carparks from CSV", e);
        }
    }

    private Carpark buildFrom(CSVRecord csvRecord) {
        ProjCoordinate coordinate = CoordinateTransformUtil.transform(
            CoordinateTransformUtil.SVY21,
            CoordinateTransformUtil.WGS84,
            new ProjCoordinate(
                Double.parseDouble(csvRecord.get(X_COORDINATE)),
                Double.parseDouble(csvRecord.get(Y_COORDINATE))
            )
        );

        return new Carpark(
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
    public List<CarparkDetails> getNearestCarParks(NearestCarparksRequest request, Pageable pageable) {
        Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        Page<Carpark> carparks = carparkRepository.findNearestCarparks(location, request.distance(), pageable);

        updateCarparkAvailability(carparks.getContent());

        return carparks.stream()
            .map(this::buildFrom)
            .toList();
    }

    private void updateCarparkAvailability(List<Carpark> carparks) {
        Map<String, CarparkAvailabilityDTO> availabilityPerCarpark =
            dataGovSGService.getCarparkAvailability();

        List<Carpark> carparkWithOutdatedAvailability = carparks.stream()
            .filter(hasOutdatedAvailability(availabilityPerCarpark))
            .toList();

        if (CollectionUtils.isNotEmpty(carparkWithOutdatedAvailability)) {
            carparkWithOutdatedAvailability.forEach(updateCarparkAvailability(availabilityPerCarpark));
            carparkRepository.saveAll(carparkWithOutdatedAvailability);
        }
    }

    private Consumer<Carpark> updateCarparkAvailability(Map<String, CarparkAvailabilityDTO> availabilityPerCarpark) {
        return carparkWithOutdatedAvailability -> {
            CarparkAvailabilityDTO updatedAvailability =
                availabilityPerCarpark.get(carparkWithOutdatedAvailability.getId());
            CarparkAvailability availability = carparkWithOutdatedAvailability.getAvailability();
            availability.setTotalLots(updatedAvailability.totalLots());
            availability.setAvailableLots(updatedAvailability.availableLots());
        };
    }

    private Predicate<Carpark> hasOutdatedAvailability(Map<String, CarparkAvailabilityDTO> availabilityPerCarpark) {
        return carpark -> {
            CarparkAvailabilityDTO updatedAvailability = availabilityPerCarpark.get(carpark.getId());
            return updatedAvailability != null && carpark.getAvailability().getUpdatedAt()
                .isBefore(updatedAvailability.lastUpdated());
        };
    }

    private CarparkDetails buildFrom(Carpark carpark) {
        return new CarparkDetails(
            carpark.getAddress(),
            carpark.getLocation().getY(),
            carpark.getLocation().getX(),
            carpark.getAvailability().getTotalLots(),
            carpark.getAvailability().getAvailableLots()
        );
    }
}
