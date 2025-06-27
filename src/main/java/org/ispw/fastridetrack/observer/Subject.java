package org.ispw.fastridetrack.observer;

import java.beans.PropertyChangeListener;

public interface Subject {
    void addObserver(PropertyChangeListener listener);
    void removeObserver(PropertyChangeListener listener);
}


