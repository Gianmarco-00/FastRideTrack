module org.ispw.fastridetrack {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.web;
    requires mysql.connector.j;
    requires jakarta.mail;
    requires org.json;
    requires com.google.gson;
    requires java.net.http;
    requires java.desktop;


    opens org.ispw.fastridetrack to javafx.fxml;
    opens org.ispw.fastridetrack.controller.guicontroller to javafx.fxml;

    exports org.ispw.fastridetrack;
    exports org.ispw.fastridetrack.controller.guicontroller;
}

