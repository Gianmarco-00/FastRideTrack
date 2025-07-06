package org.ispw.fastridetrack.bean;

import org.ispw.fastridetrack.model.Location;

public class LocationBean {
    private CoordinateBean coordinate;
    private String address;

    public LocationBean(CoordinateBean coordinate, String address) {
        this.coordinate = coordinate;
        this.address = address;
    }

    public LocationBean(CoordinateBean coordinate) {
        this.coordinate = coordinate;
        this.address = null;
    }

    public LocationBean(String address) {
        this.coordinate = null;
        this.address = address;
    }

    public CoordinateBean getCoordinate() {
        return coordinate;
    }

    public String getAddress() {
        return address;
    }

    public boolean hasAddress() {
        return address != null && !address.isBlank();
    }

    public boolean hasCoordinates() {
        return coordinate != null &&
                !Double.isNaN(coordinate.getLatitude()) &&
                !Double.isNaN(coordinate.getLongitude());
    }

    @Override
    public String toString() {
        if (hasAddress()) {
            return address;
        }
        if (hasCoordinates()) {
            return String.format("[%f, %f]", coordinate.getLatitude(), coordinate.getLongitude());
        }
        return "N/A";
    }

    public static LocationBean fromModel(Location location) {
        if (location == null) {
            return null;
        }
        return new LocationBean(
                CoordinateBean.fromModel(location.getCoordinate()),
                location.getAddress()
        );
    }
}
