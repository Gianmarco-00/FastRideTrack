package org.ispw.fastridetrack.bean;

import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.model.enumeration.RideConfirmationStatus;
import org.ispw.fastridetrack.model.TaxiRideConfirmation;

import java.time.LocalDateTime;

public class TaxiRideConfirmationBean {
    private Integer rideID;
    private DriverBean driver;
    private ClientBean client;
    private CoordinateBean userLocation;
    private String destination;
    private RideConfirmationStatus status;
    private Double estimatedFare;
    private Double estimatedTime;
    private PaymentMethod paymentMethod;
    private LocalDateTime confirmationTime;

    @SuppressWarnings("java:S107")
    public TaxiRideConfirmationBean(Integer rideID, DriverBean driver, ClientBean client,
                                    CoordinateBean userLocation, String destination, RideConfirmationStatus status,
                                    Double estimatedFare, Double estimatedTime, PaymentMethod paymentMethod,
                                    LocalDateTime confirmationTime) {
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

    public TaxiRideConfirmationBean() {}


    public Integer getRideID() {
        return rideID;
    }

    public void setRideID(Integer rideID) {
        this.rideID = rideID;
    }

    public DriverBean getDriver() {
        return driver;
    }

    public void setDriver(DriverBean driver) {
        this.driver = driver;
    }

    public ClientBean getClient() {
        return client;
    }

    public void setClient(ClientBean client) {
        this.client = client;
    }

    public CoordinateBean getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(CoordinateBean userLocation) {
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

    // Conversione da Model a Bean
    public static TaxiRideConfirmationBean fromModel(TaxiRideConfirmation model) {
        if (model == null) return null;

        return new TaxiRideConfirmationBean(
                model.getRideID(),
                DriverBean.fromModel(model.getDriver()),
                ClientBean.fromModel(model.getClient()),
                model.getUserLocation() != null ? new CoordinateBean(model.getUserLocation()) : null,
                model.getDestination(),
                model.getStatus(),
                model.getEstimatedFare(),
                model.getEstimatedTime(),
                model.getPaymentMethod(),
                model.getConfirmationTime()
        );
    }

    // Conversione da Bean a Model
    public TaxiRideConfirmation toModel() {
        return new TaxiRideConfirmation(
                rideID,
                driver != null ? driver.toModel() : null,
                client != null ? client.toModel() : null,
                userLocation != null ? userLocation.toModel() : null,
                destination,
                status,
                estimatedFare,
                estimatedTime,
                paymentMethod,
                confirmationTime
        );
    }

    // Imposto lo stato su "PENDING"
    public void markPending() {
        this.status = RideConfirmationStatus.valueOf("PENDING");
    }
}

