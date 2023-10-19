package com.tj.wegocodingexercise.util;

import org.junit.jupiter.api.Test;
import org.locationtech.proj4j.ProjCoordinate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

class CoordinateTransformUtilTest {

    private static final String SVY21 = "EPSG:3414";
    private static final String WGS84 = "EPSG:4326";

    @Test
    void transform_svy21_to_wgs84() {
        ProjCoordinate coordinate1 = new ProjCoordinate(34955.3741, 39587.9068);
        ProjCoordinate result1 = CoordinateTransformUtil.transform(SVY21, WGS84, coordinate1);
        assertThat(result1.x).isEqualTo(103.8958, withPrecision(0.0001d));
        assertThat(result1.y).isEqualTo(1.3742, withPrecision(0.0001d));

        ProjCoordinate coordinate2 = new ProjCoordinate(35318.5017, 39372.252);
        ProjCoordinate result2 = CoordinateTransformUtil.transform(SVY21, WGS84, coordinate2);
        assertThat(result2.x).isEqualTo(103.8990, withPrecision(0.0001d));
        assertThat(result2.y).isEqualTo(1.3723, withPrecision(0.0001d));

        ProjCoordinate coordinate3 = new ProjCoordinate(35112.2965, 39125.1711);
        ProjCoordinate result3 = CoordinateTransformUtil.transform(SVY21, WGS84, coordinate3);
        assertThat(result3.x).isEqualTo(103.8972, withPrecision(0.0001d));
        assertThat(result3.y).isEqualTo(1.3701, withPrecision(0.0001d));
    }
}