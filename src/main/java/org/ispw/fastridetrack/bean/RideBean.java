package org.ispw.fastridetrack.bean;

import org.ispw.fastridetrack.model.Ride;
import org.ispw.fastridetrack.model.enumeration.RideStatus;

import java.time.LocalDateTime;

public class RideBean {
    private Integer rideID;
    private ClientBean client;
    private DriverBean driver;
    private String destination;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double totalPayed;
    private Boolean clientFetched;
    private RideStatus status;

    // Costruttore completo
    @SuppressWarnings("java:S107")
    public RideBean(Integer rideID, ClientBean client, DriverBean driver,
                    String destination, LocalDateTime startTime,
                    LocalDateTime endTime, Double totalPayed, Boolean clientFetched, RideStatus status) {
        this.rideID = rideID;
        this.driver = driver;
        this.client = client;
        this.destination = destination;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalPayed = totalPayed;
        this.clientFetched = clientFetched;
        this.status = status;
    }

    public static RideBean fromModel(Ride model) {
        if (model == null) return null;

        return new RideBean(
                model.getRideID(),
                ClientBean.fromModel(model.getClient()),
                DriverBean.fromModel(model.getDriver()),
                model.getDestination(),
                model.getStartTime(),
                model.getEndTime(),
                model.getTotalPayed(),
                model.isClientFetched(),
                model.getStatus()
        );
    }

    public Ride toModel() {
        return new Ride(
                rideID,
                client.toModel(),
                driver.toModel(),
                destination,
                startTime,
                endTime,
                totalPayed,
                clientFetched,
                status
        );
    }

    public Integer getRideID() {return rideID;}

    public  void setRideID(Integer rideID) {this.rideID = rideID;}

    public DriverBean getDriver() {return driver;}

    public void setDriver(DriverBean driver) {this.driver = driver;}

    public ClientBean getClient() {return client;}

    public void setClient(ClientBean client) {this.client = client;}

    public String getDestination() {return destination;}

    public void setDestination(String destination) {this.destination = destination;}

    public LocalDateTime getStartTime() {return startTime;}

    public void setStartTime(LocalDateTime startTime) {this.startTime = startTime;}

    public LocalDateTime getEndTime() {return endTime;}

    public void setEndTime(LocalDateTime endTime) {this.endTime = endTime;}

    public Double getTotalPayed() {return totalPayed;}

    public void setTotalPayed(Double totalPayed) {this.totalPayed = totalPayed;}

    public RideStatus getRideStatus() {return status;}

    public void setRideStatus(RideStatus status) {this.status = status;}

}
