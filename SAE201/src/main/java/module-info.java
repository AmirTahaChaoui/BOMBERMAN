module com.example.sae201 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.bomberman to javafx.fxml;
    exports com.bomberman;
}