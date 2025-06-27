package org.ispw.fastridetrack.model;

import org.ispw.fastridetrack.model.enumeration.RideStatus;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class Ride {
    private Integer rideID;
    private Client client;
    private Driver driver;
    private String destination;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalPayed;
    private RideStatus status;

    public Ride() {}

    public Ride(Integer rideID, Client client, Driver driver, String destination,
                LocalDateTime startTime, LocalDateTime endTime, BigDecimal totalPayed, RideStatus status) {
        this.rideID = rideID;
        this.client = client;
        this.driver = driver;
        this.destination = destination;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalPayed = totalPayed;
        this.status = status;
    }

    public void startRide() {
        this.startTime = LocalDateTime.now();
        this.status = RideStatus.ONGOING;
    }

    public void finishRide(BigDecimal fare) {
        this.endTime = LocalDateTime.now();
        Duration rideDuration = Duration.between(startTime, endTime);
        this.totalPayed = fare;
        this.status = RideStatus.FINISHED;
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

    public BigDecimal getTotalPayed() { return totalPayed;}

    public void setTotalPayed(BigDecimal totalPayed) {this.totalPayed = totalPayed;}

    public RideStatus getStatus() {return status;}

    public void setStatus(RideStatus status) {this.status = status;}
}
