package org.ispw.fastridetrack.model.state;

import org.ispw.fastridetrack.model.Location;
import org.ispw.fastridetrack.model.enumeration.RideStatus;


public interface RideState {

    Location getMapStartPoint(RideContext rideContext);
    Location getMapEndPoint(RideContext rideContext);
    RideStatus getRideStatus();
    RideState markClientFound();
    RideState startRide();
    RideState markFinished(Double totalPayed);
}
