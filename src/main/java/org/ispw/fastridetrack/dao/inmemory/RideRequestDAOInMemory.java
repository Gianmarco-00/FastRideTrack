package org.ispw.fastridetrack.dao.inmemory;

import org.ispw.fastridetrack.model.RideRequest;
import org.ispw.fastridetrack.dao.RideRequestDAO;

import java.util.HashMap;
import java.util.Map;

public class RideRequestDAOInMemory implements RideRequestDAO {
    private final Map<Integer, RideRequest> storage = new HashMap<>();

    @Override
    public RideRequest save(RideRequest rideRequest) {
        if (rideRequest.getRequestId() == null || rideRequest.getRequestId() == 0) {
            // Genera un nuovo ID progressivo (non thread-safe, ma sufficiente per demo)
            rideRequest.setRequestId(storage.size() + 1);
        }
        storage.put(rideRequest.getRequestId(), rideRequest);
        return rideRequest;
    }

    @Override
    public RideRequest findById(int requestID) {
        return storage.get(requestID);
    }

    @Override
    public void update(RideRequest rideRequest) {
        storage.put(rideRequest.getRequestId(), rideRequest);
    }
}

