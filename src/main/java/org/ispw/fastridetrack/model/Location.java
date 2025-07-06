package org.ispw.fastridetrack.model;

public class Location {
    private Coordinate coordinate;
    private String address;

    public Location(Coordinate coordinate, String address) {
        this.coordinate = coordinate;
        this.address = address;
    }

    public Location(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.address = null;
    }

    public Location(String address) {
        this.coordinate = null;
        this.address = address;
    }

    public Coordinate getCoordinate() {
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
}
