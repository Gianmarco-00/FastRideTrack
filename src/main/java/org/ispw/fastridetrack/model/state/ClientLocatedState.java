package org.ispw.fastridetrack.model.state;

import org.ispw.fastridetrack.model.Location;
import org.ispw.fastridetrack.model.enumeration.RideStatus;


public class ClientLocatedState implements RideState {

    @Override
    public Location getMapStartPoint(RideContext ctx) {
        return new Location(ctx.getDriverCoord());
    }

    @Override
    public Location getMapEndPoint(RideContext ctx) {
        return  new Location(ctx.getClientCoord());
    }

    @Override
    public RideStatus getRideStatus() {
        return RideStatus.CLIENT_LOCATED;
    }

    @Override
    public RideState markClientFound() {
        throw new IllegalStateException("Client already found");
    }

    @Override
    public RideState startRide() {
        return new OnGoingState();
    }

    @Override
    public RideState markFinished(Double total) {
        throw new IllegalStateException("Ride not yet started");
    }
}
