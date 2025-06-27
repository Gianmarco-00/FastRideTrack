package org.ispw.fastridetrack.model;

import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.model.enumeration.RideConfirmationStatus;

import java.time.LocalDateTime;
import java.util.Objects;

public class TaxiRideConfirmation {
    private Integer rideID;
    private Driver driver;
    private Client client;
    private Coordinate userLocation;
    private String destination;
    private RideConfirmationStatus status;
    private Double estimatedFare;
    private Double estimatedTime;
    private PaymentMethod paymentMethod;
    private LocalDateTime confirmationTime;

    public TaxiRideConfirmation() {}

    @SuppressWarnings("java:S107")
    public TaxiRideConfirmation(Integer rideID, Driver driver, Client client, Coordinate userLocation,
                                String destination, RideConfirmationStatus status, Double estimatedFare,
                                Double estimatedTime, PaymentMethod paymentMethod, LocalDateTime confirmationTime) {
        this.rideID = rideID;
        this.driver = driver;
        this.client = client;
        this.userLocation = userLocation;
        this.destination = destination;
        this.status = status;
        this.estimatedFare = estimatedFare;
        this.estimatedTime = estimatedTime;
        this.paymentMethod = paymentMethod;
        this.confirmationTime = confirmationTime;
    }

    // Getters e setters
    public Integer getRideID() {
        return rideID;
    }

    public void setRideID(Integer rideID) {
        this.rideID = rideID;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Coordinate getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Coordinate userLocation) {
        this.userLocation = userLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public RideConfirmationStatus getStatus() {
        return status;
    }

    public void setStatus(RideConfirmationStatus status) {
        this.status = status;
    }

    public Double getEstimatedFare() {
        return estimatedFare;
    }

    public void setEstimatedFare(Double estimatedFare) {
        this.estimatedFare = estimatedFare;
    }

    public Double getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(Double estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getConfirmationTime() {
        return confirmationTime;
    }

    public void setConfirmationTime(LocalDateTime confirmationTime) {
        this.confirmationTime = confirmationTime;
    }

    @Override
    public String toString() {
        return "TaxiRideConfirmation{" +
                "rideID=" + rideID +
                ", driver=" + (driver != null ? driver.getUsername() : "null") +
                ", client=" + (client != null ? client.getUsername() : "null") +
                ", userLocation=" + (userLocation != null ? userLocation.toString() : "null") +
                ", destination='" + destination + '\'' +
                ", status='" + status + '\'' +
                ", estimatedFare=" + estimatedFare +
                ", estimatedTime=" + estimatedTime +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", confirmationTime=" + confirmationTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaxiRideConfirmation that = (TaxiRideConfirmation) o;
        return Objects.equals(rideID, that.rideID);
    }

    @Override
    public int hashCode() {
        return rideID != null ? rideID.hashCode() : 0;
    }
}


