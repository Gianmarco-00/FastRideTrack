package org.ispw.fastridetrack.bean;

import org.ispw.fastridetrack.model.Coordinate;

public class CoordinateBean {
    private double latitude;
    private double longitude;

    public CoordinateBean(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public CoordinateBean(Coordinate coordinate) {
        if (coordinate != null) {
            this.latitude = coordinate.getLatitude();
            this.longitude = coordinate.getLongitude();
        }
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Coordinate toModel() {
        return new Coordinate(latitude, longitude);
    }

    public static CoordinateBean fromModel(Coordinate coordinate) {
        if (coordinate != null) {
            return new CoordinateBean(coordinate.getLatitude(), coordinate.getLongitude());
        }
        return null;
    }

    @Override
    public String toString() {
        return latitude + "," + longitude;
    }

}

