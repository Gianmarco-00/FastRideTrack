package org.ispw.fastridetrack.model.state;

import org.ispw.fastridetrack.model.Location;
import org.ispw.fastridetrack.model.enumeration.RideStatus;


public class InitiatedState implements RideState {

    @Override
    public Location getMapStartPoint(RideContext ctx) {
        return new Location(ctx.getDriverCoord());
    }

    @Override
    public Location getMapEndPoint(RideContext ctx) {
        return new Location(ctx.getClientCoord());
    }

    @Override
    public RideStatus getRideStatus() {
        return RideStatus.INITIATED;
    }

    @Override
    public RideState markClientFound() {
        return new ClientLocatedState();
    }

    @Override
    public RideState startRide() {
        throw new IllegalStateException("Client not yet found");
    }

    @Override
    public RideState markFinished(Double total) {
        throw new IllegalStateException("Ride not yet started");
    }
}
