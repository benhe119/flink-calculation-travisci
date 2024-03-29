package com.examples.utils;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.table.functions.ScalarFunction;

/**
 * GeoUtils provides utility methods to deal with locations in New York City.
 */
public class GeoUtils {

    // geo boundaries of the area of NYC
    private static double LON_EAST = -73.7;
    private static double LON_WEST = -74.05;
    private static double LAT_NORTH = 41.0;
    private static double LAT_SOUTH = 40.5;

    // delta step to create artificial grid overlay of NYC
    private static double DELTA_LON = 0.0014;
    private static double DELTA_LAT = 0.00125;

    // ( |LON_WEST| - |LON_EAST| ) / DELTA_LAT
    private static int NUMBER_OF_GRID_X = 250;
    // ( LAT_NORTH - LAT_SOUTH ) / DELTA_LON
    private static int NUMBER_OF_GRID_Y = 400;

    /**
     * Checks if a location specified by longitude and latitude values is
     * within the geo boundaries of New York City.
     *
     * @param lon longitude of the location to check
     * @param lat latitude of the location to check
     *
     * @return true if the location is within NYC boundaries, otherwise false.
     */
    public static boolean isInNYC(float lon, float lat) {

        return !(lon > LON_EAST || lon < LON_WEST) &&
                !(lat > LAT_NORTH || lat < LAT_SOUTH);
    }

    /**
     * Maps a location specified by latitude and longitude values to a cell of a
     * grid covering the area of NYC.
     * The grid cells are roughly 100 x 100 m and sequentially number from north-west
     * to south-east starting by zero.
     *
     * @param lon longitude of the location to map
     * @param lat latitude of the location to map
     *
     * @return id of mapped grid cell.
     */
    public static int mapToGridCell(float lon, float lat) {
        int xIndex = (int) Math.floor((Math.abs(LON_WEST) - Math.abs(lon)) / DELTA_LON);
        int yIndex = (int) Math.floor((LAT_NORTH - lat) / DELTA_LAT);

        return xIndex + (yIndex * NUMBER_OF_GRID_X);
    }

    /**
     * Returns the longitude of the center of a grid cell.
     *
     * @param gridCellId The grid cell.
     *
     * @return The longitude value of the cell's center.
     */
    public static float getGridCellCenterLon(int gridCellId) {

        int xIndex = gridCellId % NUMBER_OF_GRID_X;

        return (float) (Math.abs(LON_WEST) - (xIndex * DELTA_LON) - (DELTA_LON / 2)) * -1.0f;
    }

    /**
     * Returns the latitude of the center of a grid cell.
     *
     * @param gridCellId The grid cell.
     *
     * @return The latitude value of the cell's center.
     */
    public static float getGridCellCenterLat(int gridCellId) {

        int xIndex = gridCellId % NUMBER_OF_GRID_X;
        int yIndex = (gridCellId - xIndex) / NUMBER_OF_GRID_X;

        return (float) (LAT_NORTH - (yIndex * DELTA_LAT) - (DELTA_LAT / 2));
    }

    /**
     * Table API / SQL Scalar UDF to check if a coordinate is in NYC.
     */
    public static class IsInNYC extends ScalarFunction {
        public boolean eval(float lon, float lat) {
            return isInNYC(lon, lat);
        }
    }

    /**
     * Table API / SQL Scalar UDF to convert a lon/lat pair into a cell ID.
     */
    public static class ToCellId extends ScalarFunction {
        public int eval(float lon, float lat) {
            return GeoUtils.mapToGridCell(lon, lat);
        }
    }

    /**
     * Table API / SQL Scalar UDF to convert a cell ID into a lon/lat pair.
     */
    public static class ToCoords extends ScalarFunction {
        public Tuple2<Float, Float> eval(int cellId) {
            return Tuple2.of(
                    GeoUtils.getGridCellCenterLon(cellId),
                    GeoUtils.getGridCellCenterLat(cellId)
            );
        }

        @Override
        public TypeInformation getResultType(Class[] signature) {
            return new TupleTypeInfo<>(Types.FLOAT, Types.FLOAT);
        }
    }
}

