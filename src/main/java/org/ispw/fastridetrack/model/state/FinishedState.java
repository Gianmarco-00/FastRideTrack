package org.ispw.fastridetrack.model.state;

import org.ispw.fastridetrack.model.Location;
import org.ispw.fastridetrack.model.enumeration.RideStatus;


public class FinishedState implements RideState{

    @Override
    public Location getMapStartPoint(RideContext rideContext) {
        return null;
    }

    @Override
    public Location getMapEndPoint(RideContext rideContext) {
        return null;
    }

    @Override
    public RideStatus getRideStatus() {
        return RideStatus.FINISHED;
    }


    @Override
    public RideState markClientFound() {
        throw new IllegalStateException("Ride already finished");
    }

    @Override
    public RideState startRide() {
        throw new IllegalStateException("Ride already finished");
    }

    @Override
    public RideState markFinished(Double totalPayed) {
        throw new IllegalStateException("Ride already finished");
    }
}
