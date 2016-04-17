package com.caseweek.comarchbeacon.cbdemo;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.Comparator;
import java.util.UUID;

/**
 * A simple struct representation of a Beacon object. Supports basic parsing and serialization
 * to and from JSONObject.
 */
public class CBeacon {

    public static final String UUID = "uuid";
    public static final String MAJOR = "major";
    public static final String MINOR = "minor";
    public static final String COLOR = "color";

    Long id;
    String uuid;
    Integer major;
    Integer minor;
    int txPower;
    double accuracy;
    String distance;
    String description;

    // This isn't really a beacon property, but it's useful to have it here so we can sort
    // the list of beacons during scanning so the closest and/or strongest is listed first.
    // It doesn't need to be persisted via the parcelable.
    int rssi;

    public CBeacon(String uuid, Integer major, Integer minor, Integer txPower, Integer rssi, String description) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.txPower = txPower;
        this.rssi = rssi;
        this.accuracy = calculateDistance(txPower, rssi);
        this.distance = getDistanceValue(this.accuracy);
        this.description = description;
    }

    public CBeacon(Cursor c) {
        id = c.getLong(c.getColumnIndex(BaseColumns._ID));
        uuid = c.getString(c.getColumnIndex(UUID));
        major = c.getInt(c.getColumnIndex(MAJOR));
        minor = c.getInt(c.getColumnIndex(MINOR));
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(UUID, uuid);
        cv.put(MAJOR, major);
        cv.put(MINOR, minor);
        //cv.put(STATUS, status);
        return cv;
    }

    public double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }

    private String getDistanceValue(Double accuracy) {
        if (accuracy == -1.0) {
            return "Unknown";
        } else if (accuracy < 1.0) {
            return "Immediate";
        } else if (accuracy < 3.0) {
            return "Near";
        } else {
            return "Far";
        }
    }


    public static final Comparator<CBeacon> RSSI_COMPARATOR = new Comparator<CBeacon>() {
        @Override
        public int compare(CBeacon lhs, CBeacon rhs) {
            return ((Integer) rhs.rssi).compareTo(lhs.rssi);
        }
    };

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getMajor() {
        return major;
    }

    public void setMajor(Integer major) {
        this.major = major;
    }

    public Integer getMinor() {
        return minor;
    }

    public void setMinor(Integer minor) {
        this.minor = minor;
    }

    public int getTxPower() {
        return txPower;
    }

    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}