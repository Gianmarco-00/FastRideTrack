package org.ispw.fastridetrack.bean;

import org.ispw.fastridetrack.model.Client;
import org.ispw.fastridetrack.model.Coordinate;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.model.enumeration.UserType;

public class ClientBean extends UserBean {
    private final PaymentMethod paymentMethod;
    @SuppressWarnings("java:S107")
    public ClientBean(String username, String password, Integer userID, String name,
                      String email, String phoneNumber, double latitude, double longitude, PaymentMethod paymentMethod) {
        super(username, password, UserType.CLIENT, userID, name, email, phoneNumber, latitude, longitude);
        this.paymentMethod = paymentMethod;
    }
    @SuppressWarnings("java:S107")
    public ClientBean(String username, String password, int userID, String name,
                      String email, String phoneNumber,
                      CoordinateBean coordinate,
                      PaymentMethod paymentMethod) {
        this(username, password, userID, name, email, phoneNumber,
                coordinate.getLatitude(), coordinate.getLongitude(),
                paymentMethod);
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }


    public static ClientBean fromModel(Client client) {
        if (client == null) return null;

        return new ClientBean(
                client.getUsername(),
                client.getPassword(),
                client.getUserID(),
                client.getName(),
                client.getEmail(),
                client.getPhoneNumber(),
                new CoordinateBean(client.getCoordinate()),
                client.getPaymentMethod()
        );
    }

    public Client toModel() {
        Coordinate coordinate = getCoordinate() != null ? getCoordinate().toModel() : null;
        return new Client(
                getUserID(),
                getUsername(),
                getPassword(),
                getName(),
                getEmail(),
                getPhoneNumber(),
                coordinate,
                getPaymentMethod()
        );

    }
}


