package org.ispw.fastridetrack.session;

import org.ispw.fastridetrack.model.Ride;
import org.ispw.fastridetrack.model.TaxiRideConfirmation;

public class DriverSessionContext {

    private TaxiRideConfirmation currentConfirmation;
    private Ride currentRide;

    // RideConfirmation accettata ma non ancora avviata
    public void setCurrentConfirmation(TaxiRideConfirmation confirmation) {
        this.currentConfirmation = confirmation;
    }

    public TaxiRideConfirmation getCurrentConfirmation() {
        return currentConfirmation;
    }

    // Ride in corso
    public void setCurrentRide(Ride ride) {
        this.currentRide = ride;
    }

    public Ride getCurrentRide() {
        return currentRide;
    }

    // True se il driver ha una conferma attiva accettata
    public boolean hasPendingConfirmation() {
        return currentConfirmation != null;
    }

    // True se la corsa è attualmente attiva
    public boolean hasActiveRide() {
        return currentRide != null;
    }

    // Resetta lo stato temporaneo della sessione (es. al termine della corsa)
    public void clear() {
        this.currentConfirmation = null;
        this.currentRide = null;
    }
}
