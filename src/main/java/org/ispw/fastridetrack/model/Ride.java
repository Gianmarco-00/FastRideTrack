package org.ispw.fastridetrack.model;

import org.ispw.fastridetrack.model.enumeration.RideStatus;
import org.ispw.fastridetrack.model.state.*;

import java.time.LocalDateTime;

@SuppressWarnings("java:S107")
public class Ride {
    private Integer rideID;
    private Client client;
    private Driver driver;
    private String destination;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double totalPayed;
    private boolean clientFetched;
    private RideStatus status;

    private RideState rideState;

    public Ride() {
    }

    public Ride(Integer rideID, Client client, Driver driver, String destination,
                LocalDateTime startTime, LocalDateTime endTime, Double totalPayed,Boolean clientFetched, RideStatus status) {
        this.rideID = rideID;
        this.client = client;
        this.driver = driver;
        this.destination = destination;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalPayed = totalPayed;
        this.clientFetched = clientFetched;
        this.status = status;

        setStatusAndState(status);
    }

    public Location getMapStartPoint() {
        return rideState.getMapStartPoint(toContext());
    }

    public Location getMapEndPoint() {
        return rideState.getMapEndPoint(toContext());
    }

    public void markClientFound() {
        this.rideState = rideState.markClientFound();
        this.status = rideState.getRideStatus();
    }

    public void startRide() {
        this.rideState = rideState.startRide();
        this.status = rideState.getRideStatus();
    }

    public void finishRide(Double totalPayed) {
        this.rideState = rideState.markFinished(totalPayed);
        this.totalPayed = totalPayed;
        this.endTime = LocalDateTime.now();
        this.status = rideState.getRideStatus();
    }

    private RideContext toContext() {
        return new RideContext(
                driver.getCoordinate(),
                client.getCoordinate(),
                destination
        );
    }

    public Integer getRideID() {return rideID;}

    public void setRideID(Integer rideID) {this.rideID = rideID;}

    public Client getClient() {return client;}

    public void setClient(Client client) {this.client = client;}

    public Driver getDriver() {return driver;}

    public void setDriver(Driver driver) {this.driver = driver;}

    public String getDestination() {return destination;}

    public void setDestination(String destination) {this.destination = destination;}

    public LocalDateTime getStartTime() {return startTime;}

    public void setStartTime(LocalDateTime startTime) {this.startTime = startTime;}

    public LocalDateTime getEndTime() { return endTime; }

    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Double getTotalPayed() { return totalPayed;}

    public void setTotalPayed(Double totalPayed) {this.totalPayed = totalPayed;}

    public void setClientFetched(Boolean clientFetched) {this.clientFetched = clientFetched;}

    public boolean isClientFetched() {
        return clientFetched;
    }

    public RideStatus getStatus() {return status;}

    public  void setStatus(RideStatus status) {this.status = status;}

    public void setStatusAndState(RideStatus status) {
        this.status = status;
        switch (status){
            case INITIATED -> this.rideState = new InitiatedState();
            case CLIENT_LOCATED ->  this.rideState = new ClientLocatedState();
            case ONGOING ->  this.rideState = new OnGoingState();
            case FINISHED -> this.rideState = new FinishedState();
        }
    }

}
