module com.example.sae201 {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires com.google.gson;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.bomberman.controller to javafx.fxml;
    opens com.bomberman to javafx.fxml;
    exports com.bomberman;

    opens com.bomberman.model to com.google.gson;
}