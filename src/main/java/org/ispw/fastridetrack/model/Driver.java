package org.ispw.fastridetrack.model;

import org.ispw.fastridetrack.model.enumeration.UserType;

public class Driver extends User {

    private String vehicleInfo;
    private String vehiclePlate;
    private String affiliation;
    private boolean available;

    @SuppressWarnings("java:S107")
    public Driver(Integer userID, String username, String password, String name, String email,
                  String phoneNumber, Coordinate coordinate, String vehicleInfo, String vehiclePlate, String affiliation, boolean available) {
        super(userID, username, password, name, email, phoneNumber, UserType.DRIVER);
        this.vehicleInfo = vehicleInfo;
        this.vehiclePlate = vehiclePlate;
        this.affiliation = affiliation;
        this.available = available;
        if (coordinate != null) {
            setLatitude(coordinate.getLatitude());
            setLongitude(coordinate.getLongitude());
        }
    }
    @SuppressWarnings("java:S107")
    public Driver(int userID, String username, String password, String name, String email, String phoneNumber,
                  double latitude, double longitude, String vehicleInfo, String vehiclePlate, String affiliation, boolean available) {
        super(userID, username, password, name, email, phoneNumber, UserType.DRIVER);
        setLatitude(latitude);
        setLongitude(longitude);
        this.vehicleInfo = vehicleInfo;
        this.vehiclePlate = vehiclePlate;
        this.affiliation = affiliation;
        this.available = available;
    }



    public String getVehicleInfo() {
        return vehicleInfo;
    }

    public void setVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }


}




