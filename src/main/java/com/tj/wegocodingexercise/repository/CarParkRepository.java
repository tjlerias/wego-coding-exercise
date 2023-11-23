package com.tj.wegocodingexercise.repository;

import com.tj.wegocodingexercise.entity.CarPark;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarParkRepository extends JpaRepository<CarPark, String> {

    @Query(
        nativeQuery = true,
        value = """
            SELECT
                c.*,
                ST_Distance(c.location, ST_SetSRID(:location, 4326)) as distance
            FROM car_park c
            WHERE ST_DWithin(c.location, ST_SetSRID(:location, 4326), :distance)
            ORDER BY distance ASC
            """
    )
    Page<CarPark> findNearestCarParks(
        @Param("location") Point location,
        @Param("distance") Integer distance,
        Pageable pageable);

    @Query(
        nativeQuery = true,
        value = """
            SELECT
                c.*,
                ST_Distance(c.location, ST_SetSRID(:location, 4326)) as distance
            FROM car_park c
            WHERE ST_DWithin(c.location, ST_SetSRID(:location, 4326), :distance)
            AND c.id not in (:unavailableCarParkIds)
            ORDER BY distance ASC
            """
    )
    Page<CarPark> findNearestCarParksExcluding(
        @Param("location") Point location,
        @Param("distance") Integer distance,
        @Param("unavailableCarParkIds") List<String> unavailableCarParkIds,
        Pageable pageable);
}
