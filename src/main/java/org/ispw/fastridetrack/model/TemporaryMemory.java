package org.ispw.fastridetrack.model;

import org.ispw.fastridetrack.bean.*;
import org.ispw.fastridetrack.model.enumeration.PaymentMethod;
import org.ispw.fastridetrack.observer.Subject;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.List;

//Questa classe funge da ConcreteSubject
public class TemporaryMemory implements Subject {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private TemporaryMemory() {}

    private static class Holder {
        private static final TemporaryMemory INSTANCE = new TemporaryMemory();
    }

    public static TemporaryMemory getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void addObserver(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void removeObserver(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    // --- proprietà osservabili ---

    private MapRequestBean mapRequestBean;
    public void setMapRequestBean(MapRequestBean bean) {
        MapRequestBean old = this.mapRequestBean;
        this.mapRequestBean = bean;
        pcs.firePropertyChange("mapRequestBean", old, bean);
    }
    public MapRequestBean getMapRequestBean() { return mapRequestBean; }

    private List<AvailableDriverBean> availableDrivers;
    public void setAvailableDrivers(List<AvailableDriverBean> drivers) {
        List<AvailableDriverBean> old = this.availableDrivers;
        this.availableDrivers = drivers;
        pcs.firePropertyChange("availableDrivers", old, drivers);
    }
    public List<AvailableDriverBean> getAvailableDrivers() { return availableDrivers; }

    private AvailableDriverBean selectedDriver;
    public void setSelectedDriver(AvailableDriverBean driver) {
        AvailableDriverBean old = this.selectedDriver;
        this.selectedDriver = driver;
        pcs.firePropertyChange("selectedDriver", old, driver);
    }
    public AvailableDriverBean getSelectedDriver() { return selectedDriver; }

    private PaymentMethod selectedPaymentMethod;
    public void setSelectedPaymentMethod(String method) {
        PaymentMethod old = this.selectedPaymentMethod;
        this.selectedPaymentMethod = PaymentMethod.valueOf(method);
        pcs.firePropertyChange("selectedPaymentMethod", old, this.selectedPaymentMethod);
    }
    public PaymentMethod getSelectedPaymentMethod() { return selectedPaymentMethod; }

    private TaxiRideConfirmationBean rideConfirmation;
    public void setRideConfirmation(TaxiRideConfirmationBean confirmation) {
        TaxiRideConfirmationBean old = this.rideConfirmation;
        this.rideConfirmation = confirmation;
        pcs.firePropertyChange("rideConfirmation", old, confirmation);
    }
    public TaxiRideConfirmationBean getRideConfirmation() { return rideConfirmation; }

    public void clear() {
        setMapRequestBean(null);
        setAvailableDrivers(null);
        setSelectedDriver(null);
        this.selectedPaymentMethod = null;
        setRideConfirmation(null);
        System.out.println("TemporaryMemory cleared!");
    }
}





