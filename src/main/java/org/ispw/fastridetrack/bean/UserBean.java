package org.ispw.fastridetrack.bean;

import org.ispw.fastridetrack.model.enumeration.UserType;

public class UserBean {

    private String username;
    private String password;
    private UserType userType;
    private Integer userID;
    private String name;
    private String email;
    private String phoneNumber;
    private double latitude;
    private double longitude;

    @SuppressWarnings("java:S107")
    public UserBean(String username, String password, UserType userType, Integer userID,
                    String name, String email, String phoneNumber, double latitude, double longitude) {
        this.username = username;
        this.password = password;
        this.userType = userType;
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getter e Setter
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

    public UserType getUserType() {
        return userType;
    }
    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public Integer getUserID() {
        return userID;
    }
    public void setUserID(Integer userID) {
        this.userID = userID;
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

    // Metodo helper per ottenere coordinate come oggetto
    public CoordinateBean getCoordinate() {
        return new CoordinateBean(latitude, longitude);
    }

    // Metodo helper per settare coordinate tramite oggetto
    public void setCoordinate(CoordinateBean coordinateBean) {
        if (coordinateBean != null) {
            this.latitude = coordinateBean.getLatitude();
            this.longitude = coordinateBean.getLongitude();
        }
    }
}


