package org.ispw.fastridetrack.controller.guicontroller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.ispw.fastridetrack.exception.FXMLLoadException;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

// Classe helper per la gestione della navigazione tra scene JavaFX.
public class SceneNavigator {
    private static final Logger LOGGER = Logger.getLogger(SceneNavigator.class.getName());
    private static Stage mainStage;
    private static ApplicationFacade facade;

    // Costruttore privato per evitare istanziazione
    private SceneNavigator() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void setStage(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Stage non può essere null");
        }
        mainStage = stage;
    }

    public static void setFacade(ApplicationFacade applicationFacade) {
        facade = applicationFacade;
    }

    public static ApplicationFacade getFacade() {
        return facade;
    }

    public static void switchTo(String fxmlPath, String title) throws FXMLLoadException {
        if (mainStage == null) {
            throw new IllegalStateException("Stage principale non impostato. Chiamare setStage() prima.");
        }

        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller != null && facade != null) {
                injectFacade(controller, facade);
            }

            mainStage.setTitle(title);
            mainStage.setScene(new Scene(root));
            mainStage.show();

        } catch (Exception e) {
            String msg = "Errore nel caricamento del file FXML: " + fxmlPath;
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, msg, e);
            }
            throw new FXMLLoadException(msg, e);
        }

    }

    private static void injectFacade(Object controller, ApplicationFacade facade) {
        try {
            Method[] methods = controller.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().equals("setFacade") &&
                        method.getParameterCount() == 1 &&
                        method.getParameterTypes()[0].equals(ApplicationFacade.class)) {
                    method.invoke(controller, facade);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, () -> "Facade iniettata in controller " + controller.getClass().getSimpleName());
                    }
                    return;
                }
            }
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, () -> "Metodo setFacade non trovato in " + controller.getClass().getSimpleName());
            }
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE,
                        e,
                        () -> "Errore durante l'iniezione della facade nel controller " + controller.getClass().getSimpleName());
            }
        }
    }
}






