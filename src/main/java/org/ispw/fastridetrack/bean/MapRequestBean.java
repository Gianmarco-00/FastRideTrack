package org.ispw.fastridetrack.bean;

public class MapRequestBean {
    private CoordinateBean origin;
    private String destination;
    private int radiusKm;
    private double estimatedTimeMinutes;

    // Costruttore vuoto
    public MapRequestBean() {}

    public MapRequestBean(CoordinateBean origin, String destination, int radiusKm) {
        this.origin = origin;
        this.destination = destination;
        this.radiusKm = radiusKm;
    }

    public CoordinateBean getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public int getRadiusKm() {
        return radiusKm;
    }

    public double getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }

    public void setOrigin(CoordinateBean origin) {
        this.origin = origin;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setRadiusKm(int radiusKm) {
        this.radiusKm = radiusKm;
    }

    public void setEstimatedTimeMinutes(double estimatedTimeMinutes) {
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    public String getOriginAsString() {
        return origin.getLatitude() + "," + origin.getLongitude();
    }


    @Override
    public String toString() {
        return "MapRequestBean{" +
                "origin=" + origin +
                ", destination='" + destination + '\'' +
                ", radiusKm=" + radiusKm +
                ", estimatedTimeMinutes=" + estimatedTimeMinutes +
                '}';
    }
}






