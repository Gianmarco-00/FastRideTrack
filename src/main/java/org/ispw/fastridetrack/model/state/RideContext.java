package org.ispw.fastridetrack.model.state;

import org.ispw.fastridetrack.model.Coordinate;


public class RideContext {
    private final Coordinate driverCoord;
    private final Coordinate clientCoord;
    private final String destination;

    public RideContext(Coordinate driverCoord, Coordinate clientCoord, String destination) {
        this.driverCoord = driverCoord;
        this.clientCoord = clientCoord;
        this.destination = destination;
    }

    public Coordinate getDriverCoord() {
        return driverCoord;
    }

    public Coordinate getClientCoord() {
        return clientCoord;
    }

    public String getDestination() {
        return destination;
    }
}