package org.ispw.fastridetrack.model;

import org.ispw.fastridetrack.model.enumeration.UserType;

public class User {
    private Integer userID;
    private String username;
    private String password;
    private String name;
    private String email;
    private String phoneNumber;
    private UserType userType;

    // Posizione(sia del client sia del driver)
    private double latitude;
    private double longitude;

    protected User(Integer userID, String username, String password, String name, String email,
                String phoneNumber, UserType userType) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
    }

    // Getters e Setters
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    // Supporto oggetto Coordinate
    public Coordinate getCoordinate() {
        return new Coordinate(this.latitude, this.longitude);
    }

    public void setCoordinate(Coordinate coordinate) {
        if (coordinate != null) {
            this.latitude = coordinate.getLatitude();
            this.longitude = coordinate.getLongitude();
        }
    }
}
