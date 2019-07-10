package com.examples.analyticsquery;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import com.examples.utils.GeoUtils;
import com.examples.utils.TaxiRide;
import com.examples.utils.TaxiRides;

/**
 * This example application runs a SQL query on a stream of taxi ride events.
 *
 * The query computes the average total amount paid per drop off-location and hour.
 * We discretize the coordinates of the drop-off location into cells of 250x250 meters.
 *
 * The stream of taxi ride events is read from a gzipped CSV file.
 * The file can be downloaded from
 *
 * https://drive.google.com/file/d/0B0TBL8JNn3JgTGNJTEJaQmFMbk0
 *
 * When starting the application the path to the file has to be provided as a parameter.
 */
public class AreasTotalPerHour {

    public static void main(String[] args) throws Exception {

        // check parameter
        if (args.length != 1) {
            System.err.println("Please provide the path to the taxi rides file as a parameter");
        }
        String inputPath = args[0];

        // create execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // configure event-time and watermarks
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.getConfig().setAutoWatermarkInterval(1000L);

        // create table environment
        StreamTableEnvironment tEnv = TableEnvironment.getTableEnvironment(env);
        // register user-defined function
        tEnv.registerFunction("toCellId", new GeoUtils.ToCellId());

        // get taxi ride event stream
        DataStream<TaxiRide> rides = TaxiRides.getRides(env, inputPath);
        // register taxi ride event stream as table "Rides"
        tEnv.registerDataStream(
            "Rides",
            rides,
            "medallion, licenseId, pickUpTime, dropOffTime.rowtime, " +
                "pickUpLon, pickUpLat, dropOffLon, dropOffLat, total");

        // define SQL query to compute average total per area and hour
        Table result = tEnv.sqlQuery(
            "SELECT " +
            "  toCellId(dropOffLon, dropOffLat) AS area, " +
            "  TUMBLE_START(dropOffTime, INTERVAL '1' HOUR) AS t, " +
            "  AVG(total) AS avgTotal " +
            "FROM Rides " +
            "GROUP BY " +
            "  toCellId(dropOffLon, dropOffLat), " +
            "  TUMBLE(dropOffTime, INTERVAL '1' HOUR)"
        );

        // convert result table into an append stream and print it
        tEnv.toAppendStream(result, Row.class)
            .print();

        // execute the query
        env.execute();
    }
}
