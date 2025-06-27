package org.ispw.fastridetrack.model;

import org.ispw.fastridetrack.model.enumeration.PaymentMethod;

public class RideRequest {
    private Integer requestId;
    private Client client;
    private String pickupLocation;
    private String destination;
    private Integer radiusKm;
    private PaymentMethod paymentMethod;
    private Driver driver;


    // Costruttore completo
    public RideRequest(Integer requestId, Client client, String pickupLocation, String destination,
                       Integer radiusKm, PaymentMethod paymentMethod, Driver driver) {
        this.requestId = requestId;
        this.client = client;
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.radiusKm = radiusKm;
        this.paymentMethod = paymentMethod;
        this.driver = driver;

    }

    // Getters e setters
    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Integer getRadiusKm() {
        return radiusKm;
    }

    public void setRadiusKm(Integer radiusKm) {
        this.radiusKm = radiusKm;
    }


    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }
}

