package com.tj.wegocodingexercise.repository;

import com.tj.wegocodingexercise.entity.CarPark;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarParkRepository extends JpaRepository<CarPark, String> {

    @Query(
        nativeQuery = true,
        value = """
            SELECT
                c.*,
                ST_Distance(c.location, ST_SetSRID(:location, 4326)) as distance
            FROM car_park c
            JOIN car_park_availability ca ON ca.car_park_id = c.id
            WHERE ST_DWithin(c.location, ST_SetSRID(:location, 4326), :distance)
            AND ca.available_lots > 0
            ORDER BY distance ASC
            """
    )
    Page<CarPark> findNearestCarParks(
        @Param("location") Point location,
        @Param("distance") Integer distance,
        Pageable pageable);
}
