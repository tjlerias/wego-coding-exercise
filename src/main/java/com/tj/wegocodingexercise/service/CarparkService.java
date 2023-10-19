package com.tj.wegocodingexercise.service;

import com.tj.wegocodingexercise.entity.Carpark;
import com.tj.wegocodingexercise.repository.CarparkRepository;
import com.tj.wegocodingexercise.util.CoordinateTransformUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
@Transactional(readOnly = true)
public class CarparkService {

    private Logger logger = LoggerFactory.getLogger(CarparkService.class);

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
    private static final String[] CARPARK_CSV_HEADERS = {CAR_PARK_NUMBER, ADDRESS, X_COORDINATE, Y_COORDINATE, CAR_PARK_TYPE,
        PARKING_SYSTEM_TYPE, SHORT_TERM_PARKING, FREE_PARKING, NIGHT_PARKING, CAR_PARK_DECKS,
        GANTRY_HEIGHT, CAR_PARK_BASEMENT};
    private final GeometryFactory geometryFactory = new GeometryFactory();

    private final CarparkRepository carparkRepository;

    public CarparkService(CarparkRepository carparkRepository) {
        this.carparkRepository = carparkRepository;
    }

    @Transactional
    public void loadFromCSV() {
        if (carparkRepository.count() > 0) {
            // No need to load data again
            return;
        }

        logger.info("Loading carparks from CSV file start.");

        List<Carpark> carparks = loadFromCSVResource("/data/HDBCarparkInformation.csv");

        carparkRepository.saveAll(carparks);

        logger.info("Loading carparks from CSV file end.");
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
            geometryFactory.createPoint(new Coordinate(coordinate.x, coordinate.y))
        );
    }
}
