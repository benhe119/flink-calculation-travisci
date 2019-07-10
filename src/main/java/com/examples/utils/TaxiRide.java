package com.examples.utils;

/**
 * POJO representing a taxi ride.
 *
 * All fields are public for ease of use in our example.
 */
public class TaxiRide {

    // id of the taxi
    public String medallion;
    // license of the driver
    public String licenseId;
    // time when passengers were picked up
    public long pickUpTime;
    // time when passengers were dropped off
    public long dropOffTime;
    // longitude where passengers were picked up
    public float pickUpLon;
    // latitude where passengers were picked up
    public float pickUpLat;
    // longitude where passengers were dropped off
    public float dropOffLon;
    // latitude where passengers were dropped off
    public float dropOffLat;
    // total amount paid by the passengers
    public float total;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(medallion);
        sb.append(',');
        sb.append(licenseId);
        sb.append(',');
        sb.append(pickUpTime);
        sb.append(',');
        sb.append(dropOffTime);
        sb.append(',');
        sb.append(pickUpLon);
        sb.append(',');
        sb.append(pickUpLat);
        sb.append(',');
        sb.append(dropOffLon);
        sb.append(',');
        sb.append(dropOffLat);
        sb.append(',');
        sb.append(total);
        sb.append(']');
        return sb.toString();
    }
}