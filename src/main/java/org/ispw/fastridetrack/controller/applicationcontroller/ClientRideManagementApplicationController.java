package org.ispw.fastridetrack.controller.applicationcontroller;

import jakarta.mail.MessagingException;
import org.ispw.fastridetrack.bean.EmailBean;
import org.ispw.fastridetrack.bean.TaxiRideConfirmationBean;
import org.ispw.fastridetrack.dao.TaxiRideConfirmationDAO;
import org.ispw.fastridetrack.model.TaxiRideConfirmation;
import org.ispw.fastridetrack.session.SessionManager;
import org.ispw.fastridetrack.adapter.EmailService;
import org.ispw.fastridetrack.adapter.GmailAdapter;

public class ClientRideManagementApplicationController {

    private final TaxiRideConfirmationDAO taxiRideConfirmationDAO;
    private final EmailService emailService;

    public ClientRideManagementApplicationController() {
        SessionManager session = SessionManager.getInstance();
        this.taxiRideConfirmationDAO = session.getTaxiRideDAO();
        this.emailService = new GmailAdapter();
    }


    //Confermo una corsa e invio una notifica al driver via email.
    public void confirmRideAndNotify(TaxiRideConfirmationBean bean, EmailBean email) throws MessagingException {
        bean.markPending();

        TaxiRideConfirmation model = bean.toModel();

        // Salvataggio della corsa
        if (!taxiRideConfirmationDAO.exists(model.getRideID())) {
            taxiRideConfirmationDAO.save(model);
        } else {
            taxiRideConfirmationDAO.update(model);
        }

        // Invio notifica al driver
        emailService.sendEmail(email.getRecipient(), email.getSubject(), email.getBody());
    }
}


