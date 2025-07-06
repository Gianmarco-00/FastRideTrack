package org.ispw.fastridetrack.model.state;

import org.ispw.fastridetrack.model.Location;
import org.ispw.fastridetrack.model.enumeration.RideStatus;


public class OnGoingState implements RideState{

    @Override
    public Location getMapStartPoint(RideContext rideContext) {
        return new Location(rideContext.getClientCoord());
    }

    @Override
    public Location getMapEndPoint(RideContext rideContext) {
        return new Location(rideContext.getDestination());
    }

    @Override
    public RideStatus getRideStatus() {
        return RideStatus.ONGOING;
    }

    @Override
    public RideState markClientFound() {
        throw new IllegalStateException("Client already picked up");
    }

    @Override
    public RideState startRide() {
        throw new IllegalStateException("Ride already started");
    }

    @Override
    public RideState markFinished(Double total) {
        return new FinishedState();
    }
}
