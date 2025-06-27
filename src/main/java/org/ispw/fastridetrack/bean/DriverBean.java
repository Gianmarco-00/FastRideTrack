package org.ispw.fastridetrack.bean;

import org.ispw.fastridetrack.model.Driver;
import org.ispw.fastridetrack.model.enumeration.UserType;


public class DriverBean extends UserBean {

    private final String vehicleInfo;
    private final String vehiclePlate;
    private final String affiliation;
    private final boolean available;

    @SuppressWarnings("java:S107")
    public DriverBean(String username, String password, int userID, String name,
                      String email, String phoneNumber,
                      double latitude, double longitude,
                      String vehicleInfo, String vehiclePlate, String affiliation,
                      boolean available) {
        super(username, password, UserType.DRIVER, userID, name, email, phoneNumber, latitude, longitude);
        this.vehicleInfo = vehicleInfo;
        this.vehiclePlate = vehiclePlate;
        this.affiliation = affiliation;
        this.available = available;
    }
    @SuppressWarnings("java:S107")
    public DriverBean(String username, String password, int userID, String name,
                      String email, String phoneNumber,
                      CoordinateBean coordinate,
                      String vehicleInfo, String vehiclePlate, String affiliation,
                      boolean available) {
        this(username, password, userID, name, email, phoneNumber,
                coordinate.getLatitude(), coordinate.getLongitude(),
                vehicleInfo, vehiclePlate, affiliation, available);
    }



    public String getVehicleInfo() {
        return vehicleInfo;
    }


    public String getVehiclePlate() {
        return vehiclePlate;
    }


    public String getAffiliation() {
        return affiliation;
    }


    public boolean isAvailable() {
        return available;
    }


    public static DriverBean fromModel(Driver driver) {
        if (driver == null) return null;

        return new DriverBean(
                driver.getUsername(),
                driver.getPassword(),
                driver.getUserID(),
                driver.getName(),
                driver.getEmail(),
                driver.getPhoneNumber(),
                new CoordinateBean(driver.getCoordinate()),
                driver.getVehicleInfo(),
                driver.getVehiclePlate(),
                driver.getAffiliation(),
                driver.isAvailable()
        );
    }

    public Driver toModel() {
        return new Driver(
                getUserID(),
                getUsername(),
                getPassword(),
                getName(),
                getEmail(),
                getPhoneNumber(),
                getCoordinate().toModel(),
                getVehicleInfo(),
                getVehiclePlate(),
                getAffiliation(),
                this.available
        );
    }

}


