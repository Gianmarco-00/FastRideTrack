package org.ispw.fastridetrack.bean;

import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.model.RideRequest;

public class RideRequestBean {
    private Integer requestID;
    private ClientBean client;
    private String pickupLocation;  // es. "lat,lng"
    private String destination;
    private Integer radiusKm;
    private PaymentMethod paymentMethod;
    private DriverBean driver;  // driver assegnato, inizialmente null

    // Costruttore vuoto (necessario per fromModel)
    public RideRequestBean() {}

    // Costruttore principale con CoordinateBean e conversione lat, long
    @SuppressWarnings("java:S107")
    public RideRequestBean(CoordinateBean origin, String destination, int radiusKm, PaymentMethod paymentMethod) {
        if (origin == null) throw new IllegalArgumentException("Origin cannot be null");
        if (destination == null || destination.isEmpty()) throw new IllegalArgumentException("Destination cannot be null or empty");
        if (radiusKm <= 0) throw new IllegalArgumentException("Radius must be positive");

        this.pickupLocation = origin.getLatitude() + "," + origin.getLongitude();
        this.destination = destination;
        this.radiusKm = radiusKm;
        this.paymentMethod = paymentMethod;
        this.driver = null;  // nessun driver assegnato all’inizio
    }


    // Getter e setter
    public Integer getRequestID() {
        return requestID;
    }

    public void setRequestID(Integer requestID) {
        this.requestID = requestID;
    }

    public ClientBean getClient() {
        return client;
    }

    public void setClient(ClientBean client) {
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

    public DriverBean getDriver() {
        return driver;
    }

    public void setDriver(DriverBean driver) {
        this.driver = driver;
    }

    // Static factory method: da Model a Bean
    public static RideRequestBean fromModel(RideRequest model) {
        if (model == null) return null;

        ClientBean clientBean = ClientBean.fromModel(model.getClient());
        DriverBean driverBean = DriverBean.fromModel(model.getDriver());

        RideRequestBean bean = new RideRequestBean();
        bean.setRequestID(model.getRequestId());
        bean.setClient(clientBean);
        bean.setPickupLocation(model.getPickupLocation());
        bean.setDestination(model.getDestination());
        bean.setRadiusKm(model.getRadiusKm());
        bean.setPaymentMethod(model.getPaymentMethod());
        bean.setDriver(driverBean);
        return bean;
    }


    // Metodo di istanza: da Bean a Model
    public RideRequest toModel() {
        return new RideRequest(
                requestID != null ? requestID : 0,
                client != null ? client.toModel() : null,
                pickupLocation,
                destination,
                radiusKm,
                paymentMethod,
                driver != null ? driver.toModel() : null
        );
    }

    //Restituisce l'origine come CoordinateBean, parsing da pickupLocation string.

    public CoordinateBean getOriginAsCoordinateBean() {
        if (pickupLocation == null || !pickupLocation.contains(",")) return null;
        String[] parts = pickupLocation.split(",");
        try {
            double lat = Double.parseDouble(parts[0]);
            double lng = Double.parseDouble(parts[1]);
            return new CoordinateBean(lat, lng);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "RideRequestBean{" +
                "requestID=" + requestID +
                ", client=" + client +
                ", pickupLocation='" + pickupLocation + '\'' +
                ", destination='" + destination + '\'' +
                ", radiusKm=" + radiusKm +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", driver=" + (driver != null ? driver.getName() : "null") +
                '}';
    }
}




