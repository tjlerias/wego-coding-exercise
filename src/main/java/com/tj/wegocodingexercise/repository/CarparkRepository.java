package com.tj.wegocodingexercise.repository;

import com.tj.wegocodingexercise.entity.Carpark;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarparkRepository extends CrudRepository<Carpark, String> {

    @Query(
        nativeQuery = true,
        value = """
            SELECT
                c.*,
                ST_Distance(c.location, ST_SetSRID(:location, 4326)) as distance
            FROM carpark c
            JOIN carpark_availability ca ON ca.carpark_id = c.id
            WHERE ST_DWithin(c.location, ST_SetSRID(:location, 4326), :distance)
            AND ca.available_lots > 0
            ORDER BY distance ASC
            """
    )
    List<Carpark> findNearbyCarparks(@Param("location") Point location, @Param("distance") Integer distance);
}
