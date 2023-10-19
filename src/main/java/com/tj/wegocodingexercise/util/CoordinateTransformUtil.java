package com.tj.wegocodingexercise.util;

import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

public final class CoordinateTransformUtil {

    public static final String SVY21 = "EPSG:3414";
    public static final String WGS84 = "EPSG:4326";
    private static final CRSFactory crsFactory = new CRSFactory();
    private static final CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();

    private CoordinateTransformUtil() {
    }

    /**
     * Transforms the given {@code coordinate} from the given {@code srcCRSCode} to {@code destCRSCode}.
     *
     * @param srcCRSCode  The source coordinate reference system identifier
     *                    in the format {@code <identifier>:<code>} (e.g., "EPSG:3414")
     * @param destCRSCode The destination coordinate reference system identifier
     *                    in the format {@code <identifier>:<code>} (e.g., "EPSG:4326")
     * @param coordinate  The coordinate to transform
     * @return The transformed coordinate
     */
    public static ProjCoordinate transform(String srcCRSCode, String destCRSCode, ProjCoordinate coordinate) {
        CoordinateReferenceSystem srcCRS = crsFactory.createFromName(srcCRSCode);
        CoordinateReferenceSystem destCRS = crsFactory.createFromName(destCRSCode);
        CoordinateTransform coordinateTransform = coordinateTransformFactory.createTransform(srcCRS, destCRS);
        return coordinateTransform.transform(coordinate, new ProjCoordinate());
    }
}
