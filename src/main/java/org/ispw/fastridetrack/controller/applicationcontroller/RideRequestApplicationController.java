package org.ispw.fastridetrack.controller.applicationcontroller;

import org.ispw.fastridetrack.bean.RideRequestBean;
import org.ispw.fastridetrack.dao.RideRequestDAO;
import org.ispw.fastridetrack.model.RideRequest;
import org.ispw.fastridetrack.session.SessionManager;

public class RideRequestApplicationController {

    private RideRequestDAO rideRequestDAO;

    public RideRequestApplicationController() {
        // Determina l'implementazione di RideRequestDAO in base alla variabile d'ambiente
        this.rideRequestDAO = SessionManager.getInstance().getRideRequestDAO();
    }

    // Crea una nuova richiesta di corsa
    public void createRideRequest(RideRequestBean rideRequestBean) {
        // Salva la richiesta nel dao (Database o in-memory)
        rideRequestDAO.save(rideRequestBean.toModel());
    }

    // Visualizza una richiesta di corsa
    public RideRequest viewRideRequest(int rideID) {
        return rideRequestDAO.findById(rideID);
    }
}



