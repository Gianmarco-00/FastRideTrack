package org.ispw.fastridetrack;

import javafx.application.Application;
import javafx.stage.Stage;
import org.ispw.fastridetrack.controller.clicontroller.ClientCliController;
import org.ispw.fastridetrack.controller.applicationcontroller.ApplicationFacade;
import org.ispw.fastridetrack.controller.guicontroller.SceneNavigator;
import org.ispw.fastridetrack.session.SessionManager;

import static org.ispw.fastridetrack.util.ViewPath.HOMEPAGE_FXML;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SessionManager.init();
        SceneNavigator.setStage(primaryStage);
        SceneNavigator.setFacade(new ApplicationFacade());
        SceneNavigator.switchTo(HOMEPAGE_FXML, "Homepage");
        primaryStage.setOnCloseRequest(event -> SessionManager.getInstance().shutdown());
    }

    @Override
    public void stop() throws Exception {
        SessionManager.getInstance().shutdown();
        super.stop();
    }

    public static void main(String[] args) {

        String useCliEnv=System.getenv("USE_CLI");
        boolean useCli = useCliEnv != null && useCliEnv.equalsIgnoreCase("true");
        if (useCli) {
            System.out.println("Using CLI");
            try {
                SessionManager.init();
                ClientCliController clientCliController = new ClientCliController();
                clientCliController.start();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                SessionManager.getInstance().shutdown();
            }
        } else{
            System.out.println("Using GUI");
            launch(args);
        }
    }
}


