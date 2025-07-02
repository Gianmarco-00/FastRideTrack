package org.ispw.fastridetrack.controller.applicationcontroller;

import jakarta.mail.MessagingException;
import org.ispw.fastridetrack.adapter.GmailAdapter;
import org.ispw.fastridetrack.bean.*;
import org.ispw.fastridetrack.adapter.GoogleMapsAdapter;
import org.ispw.fastridetrack.exception.*;
import org.ispw.fastridetrack.model.*;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.model.enumeration.RideConfirmationStatus;
import org.ispw.fastridetrack.model.enumeration.UserType;
import org.ispw.fastridetrack.util.DriverSessionContext;
import org.ispw.fastridetrack.session.SessionManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ApplicationFacade {

    private final LoginApplicationController loginAC;
    private final DriverMatchingApplicationController driverMatchingAC;
    private final ClientRideManagementApplicationController clientRideManagementAC;
    private final MapApplicationController mapAC;
    private final RideConfirmationApplicationController rideConfirmationAC;
    private final CurrentRideManagementApplicationController currentRideManagementAC;

    public ApplicationFacade(){
        this.rideConfirmationAC = new RideConfirmationApplicationController();
        this.currentRideManagementAC = new CurrentRideManagementApplicationController();
        this.loginAC = new LoginApplicationController();
        this.driverMatchingAC = new DriverMatchingApplicationController();
        this.clientRideManagementAC = new ClientRideManagementApplicationController();
        this.mapAC = new MapApplicationController();
    }


    public DriverMatchingApplicationController getDriverMatchingAC() {
        return driverMatchingAC;
    }
    public ClientRideManagementApplicationController getClientRideManagementAC() {
        return clientRideManagementAC;
    }
    public MapApplicationController getMapAC() {
        return mapAC;
    }


    public boolean login(String username, String password) throws ClientDAOException, DriverDAOException {
        // Provo login client
        if (loginAC.validateClientCredentials(username, password, UserType.CLIENT)) {
            return true;
        }
        // Provo login driver
        if (loginAC.validateDriverCredentials(username, password, UserType.DRIVER)) {
            if (currentRideManagementAC.getCurrentActiveRideByDriver(SessionManager.getInstance().getLoggedDriver().getUserID()) != null){
                DriverSessionContext.getInstance().setCurrentRide(currentRideManagementAC.getCurrentActiveRideByDriver(SessionManager.getInstance().getLoggedDriver().getUserID()));
            }
            return true;
        }
        // Nessuno ha validato
        return false;
    }

    public UserType getLoggedUserType() {
        if (SessionManager.getInstance().getLoggedClient() != null) return UserType.CLIENT;
        if (SessionManager.getInstance().getLoggedDriver() != null) return UserType.DRIVER;
        return null;
    }


    public String processRideRequestAndReturnMapHtml(RideRequestBean rideBean, MapRequestBean mapBean) throws RideRequestSaveException, MapServiceException {
        driverMatchingAC.saveRideRequest(rideBean);
        return mapAC.showMap(mapBean).getHtmlContent();
    }

    public void confirmRideRequest(AvailableDriverBean selectedDriver, PaymentMethod method) throws RideRequestSaveException, DriverDAOException {
        MapRequestBean mapRequestBean = TemporaryMemory.getInstance().getMapRequestBean();
        ClientBean currentClient = ClientBean.fromModel(SessionManager.getInstance().getLoggedClient());

        RideRequestBean rideRequest = new RideRequestBean(
                mapRequestBean.getOrigin(),
                mapRequestBean.getDestination(),
                mapRequestBean.getRadiusKm(),
                method
        );
        rideRequest.setDriver(selectedDriver);
        rideRequest.setClient(currentClient);

        TemporaryMemory.getInstance().setSelectedDriver(selectedDriver);
        TemporaryMemory.getInstance().setSelectedPaymentMethod(method.name());

        RideRequestBean savedRequest = driverMatchingAC.saveRideRequest(rideRequest);
        DriverAssignmentBean assignmentBean = new DriverAssignmentBean(
                savedRequest.getRequestID(),
                selectedDriver.toModel()
        );
        driverMatchingAC.assignDriverToRequest(assignmentBean);

        TaxiRideConfirmationBean confirmationBean = new TaxiRideConfirmationBean(
                savedRequest.getRequestID(),
                DriverBean.fromModel(selectedDriver.toModel()),
                currentClient,
                savedRequest.getOriginAsCoordinateBean(),
                savedRequest.getDestination(),
                RideConfirmationStatus.PENDING,
                selectedDriver.getEstimatedPrice(),
                selectedDriver.getEstimatedTime(),
                savedRequest.getPaymentMethod(),
                LocalDateTime.now()
        );

        TemporaryMemory.getInstance().setRideConfirmation(confirmationBean);
    }


    public Map loadMapAndAvailableDriversForClient() throws MapServiceException, DriverDAOException {
        TemporaryMemory memory = TemporaryMemory.getInstance();
        MapRequestBean bean = memory.getMapRequestBean();

        if (bean == null) {
            throw new IllegalStateException("Nessuna richiesta mappa trovata in memoria.");
        }

        // Mostro mappa della corsa (utente -> destinazione)
        Map rideMap = getMapAC().showMap(bean);

        List<AvailableDriverBean> baseDrivers = getDriverMatchingAC().findAvailableDrivers(bean);
        CoordinateBean userPos = bean.getOrigin();
        String destination = bean.getDestination();

        for (AvailableDriverBean driver : baseDrivers) {
            CoordinateBean driverPos = driver.getCoordinate();
            if (driverPos == null || userPos == null) continue;

            String userPosStr = userPos.getLatitude() + "," + userPos.getLongitude();
            MapRequestBean etaRequest = new MapRequestBean(driverPos, userPosStr, 0);
            Map etaMap = getMapAC().showMap(etaRequest);
            double eta = etaMap.getEstimatedTimeMinutes();

            MapRequestBean rideReq = new MapRequestBean(userPos, destination, 0);
            Map rideEstimates = getMapAC().showMap(rideReq);

            double km = rideEstimates.getDistanceKm();
            double min = rideEstimates.getEstimatedTimeMinutes();
            double estimatedFare = km * 1.0 + min * 0.20;

            driver.setEstimatedTime(eta + min);
            driver.setEstimatedPrice(estimatedFare);
        }

        memory.setAvailableDrivers(baseDrivers);

        return rideMap;
    }


    public void confirmRideAndNotifyDriver() throws MessagingException, MapServiceException {
        TaxiRideConfirmationBean rideBean = TemporaryMemory.getInstance().getRideConfirmation();

        if (rideBean == null || rideBean.getDriver() == null) {
            throw new IllegalStateException("Nessuna corsa o driver disponibile.");
        }

        GoogleMapsAdapter mapsAdapter = new GoogleMapsAdapter();

        // Ottieni indirizzo cliente
        String originAddress = "Indirizzo non disponibile";
        CoordinateBean originCoord = rideBean.getUserLocation();
        if (originCoord != null) {
            originAddress = mapsAdapter.getAddressFromCoordinates(
                    originCoord.getLatitude(), originCoord.getLongitude()
            );
        }

        DriverBean driver = rideBean.getDriver();

        String subject = "Nuova corsa: " + rideBean.getRideID();

        String body = String.format("""
        Ciao %s,

        Hai una nuova corsa assegnata.

        Cliente: %s
        Partenza: %s
        Destinazione: %s
        Tariffa stimata: €%.2f
        Tempo stimato: %.2f minuti

        Controlla l'app per maggiori dettagli.

        Grazie!
        """,
                driver.getName(),
                rideBean.getClient().getName(),
                originAddress,
                rideBean.getDestination(),
                rideBean.getEstimatedFare(),
                rideBean.getEstimatedTime()
        );

        EmailBean email = new EmailBean(driver.getEmail(), subject, body);

        getClientRideManagementAC().confirmRideAndNotify(rideBean, email);
    }


    public Optional<TaxiRideConfirmationBean> getNextRideConfirmation(Integer driverID) throws DriverDAOException  {
        return rideConfirmationAC.getNextRideConfirmation(driverID);
    }

    public void acceptRideConfirmationAndInitializeRide(TaxiRideConfirmationBean confirmationBean) throws
            DriverDAOException, MapServiceException, MessagingException, DriverUnavailableException,
            RideConfirmationNotFoundException, RideAlreadyActiveException, RideConfirmationNotPendingException {

        GoogleMapsAdapter mapsAdapter = new GoogleMapsAdapter();
        GmailAdapter gmailAdapter = new GmailAdapter();

        try {
            rideConfirmationAC.acceptRideConfirmationAndRejectOthers(confirmationBean.getRideID(), confirmationBean.getDriver().getUserID());
            RideBean rideBean = currentRideManagementAC.initializeCurrentRide(confirmationBean);

            DriverSessionContext.getInstance().setCurrentRide(rideBean);
            DriverSessionContext.getInstance().setCurrentConfirmation(confirmationBean);
            DriverSessionContext.getInstance().getCurrentConfirmation().setStatus(RideConfirmationStatus.ACCEPTED);
        }catch(DriverUnavailableException | RideConfirmationNotFoundException | RideConfirmationNotPendingException | RideAlreadyActiveException e){
            DriverSessionContext.getInstance().setCurrentConfirmation(null);
            throw e;
        }

        String originAddress = "Indirizzo non disponibile";
        CoordinateBean originCoord = confirmationBean.getUserLocation();
        if (originCoord != null) {
            originAddress = mapsAdapter.getAddressFromCoordinates(
                    originCoord.getLatitude(), originCoord.getLongitude()
            );
        }
        String subject = "Corsa accettata: " + confirmationBean.getRideID();

        String body = String.format("""
        Ciao %s,

        La tua richiesta di corsa è stata presa in carico.

        Tassista: %s
        Partenza: %s
        Destinazione: %s
        Tariffa stimata: €%.2f
        Tempo stimato: %.2f minuti

        Controlla l'app per maggiori dettagli.

        Grazie!
        """,    confirmationBean.getClient().getName(),
                confirmationBean.getDriver().getName(),
                originAddress,
                confirmationBean.getDestination(),
                confirmationBean.getEstimatedFare(),
                confirmationBean.getEstimatedTime()
        );
        gmailAdapter.sendEmail(confirmationBean.getClient().getEmail(), subject, body);
    }


    public void rejectRideConfirmation(TaxiRideConfirmationBean confirmation,String reason) throws MessagingException {

        GmailAdapter gmailAdapter = new GmailAdapter();

        rideConfirmationAC.rejectRideConfirmation(confirmation.getRideID(), confirmation.getDriver().getUserID());

        String subject = "Corsa rifiutata: " + confirmation.getRideID();

        String body = String.format("""
        Ciao %s,

        La tua richiesta di corsa è stata rifiutata.
        Motivo:
        %s

        Controlla l'app per maggiori dettagli.
        
        """,    confirmation.getClient().getName(),
                reason
        );
        gmailAdapter.sendEmail(confirmation.getClient().getEmail(), subject, body);
    }

    public boolean isActiveRide() {
        return DriverSessionContext.getInstance().hasActiveRide();
    }

    public boolean isConfirmationAccepted() {
        return  DriverSessionContext.getInstance().hasPendingConfirmation();
    }

    public Map loadDriverRouteBasedOnRideStatus() throws MapServiceException {
        RideBean currentActiveRide = DriverSessionContext.getInstance().getCurrentRide();
        LocationBean start = currentRideManagementAC.getCurrentMapStartPoint(currentActiveRide);
        LocationBean end = currentRideManagementAC.getCurrentMapEndPoint(currentActiveRide);
        return mapAC.displayMapRoute(start, end);
    }

    public Map loadDriverRoute() throws MapServiceException {
        LocationBean start = DriverSessionContext.getInstance().getStartPoint();
        LocationBean end = DriverSessionContext.getInstance().getEndPoint();
        return mapAC.displayMapRoute(start, end);
    }

    public String getAddressFromCoordinatesString(String coordinates) throws MapServiceException {
        GoogleMapsAdapter gmapsAdapter = new GoogleMapsAdapter();
        return gmapsAdapter.getAddressFromCoordinatesString(coordinates);
    }

    public void markClientLocated(double estimatedTime) throws MessagingException {
        RideBean currentRideBean = DriverSessionContext.getInstance().getCurrentRide();

        RideBean updatedRide = currentRideManagementAC.markClientLocated(currentRideBean);

        DriverSessionContext.getInstance().setCurrentRide(updatedRide);

        // 3. invii l'email
        GmailAdapter gmailAdapter = new GmailAdapter();
        String subject = "Cliente trovato: " + updatedRide.getRideID();
        String body = String.format("""
        Ciao %s,

        Il tassista ti ha localizzato ed è in arrivo alla tua posizione.

        Tassista: %s
        Tempo stimato all'arrivo: %.2f minuti
        
        Controlla l'app per maggiori dettagli.

        Grazie!
        """,
                updatedRide.getClient().getName(),
                updatedRide.getDriver().getName(),
                estimatedTime
        );

        gmailAdapter.sendEmail(updatedRide.getClient().getEmail(), subject, body);
    }

    public void startRide() throws ClientNotFetchedException{
        RideBean currentRideBean = DriverSessionContext.getInstance().getCurrentRide();

        RideBean updatedRide = currentRideManagementAC.startRide(currentRideBean);

        DriverSessionContext.getInstance().setCurrentRide(updatedRide);
    }
}
