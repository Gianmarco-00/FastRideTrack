package org.ispw.fastridetrack.bean;

import org.ispw.fastridetrack.model.Driver;

public class DriverAssignmentBean {
    private Integer requestID;
    private Driver driver;

    public DriverAssignmentBean(Integer requestID, Driver driver) {
        if (requestID == null) throw new IllegalArgumentException("requestID cannot be null");
        if (driver == null) throw new IllegalArgumentException("Driver cannot be null");

        this.requestID = requestID;
        this.driver = driver;
    }

    public DriverAssignmentBean() {}

    public Integer getRequestID() {
        return requestID;
    }

    public void setRequestID(Integer requestID) {
        this.requestID = requestID;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    @Override
    public String toString() {
        return "DriverAssignmentBean{" +
                "requestID=" + requestID +
                ", driver=" + (driver != null ? driver.getName() : "null") +
                '}';
    }
}


