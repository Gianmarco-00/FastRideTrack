package org.ispw.fastridetrack.model;

import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.model.enumeration.UserType;

public class Client extends User {
    private PaymentMethod paymentMethod;

    @SuppressWarnings("java:S107")
    public Client(Integer userID, String username, String password, String name, String email,
                  String phoneNumber, PaymentMethod paymentMethod) {
        super(userID, username, password, name, email, phoneNumber, UserType.CLIENT);
        this.paymentMethod = paymentMethod;
    }
    @SuppressWarnings("java:S107")
    public Client(Integer userID, String username, String password, String name, String email,
                  String phoneNumber, Coordinate coordinate, PaymentMethod paymentMethod) {
        this(userID, username, password, name, email, phoneNumber, paymentMethod);
        if (coordinate != null) {
            setLatitude(coordinate.getLatitude());
            setLongitude(coordinate.getLongitude());
        }
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

}
