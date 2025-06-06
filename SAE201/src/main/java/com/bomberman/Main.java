package com.bomberman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static final String TITLE = "Super Bomberman";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/theme1.fxml"));
            Parent root = loader.load();

            // Créer la scène
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

            // Charger le CSS (optionnel pour l'instant)
            scene.getStylesheets().add(getClass().getResource("/css/theme1.css").toExternalForm());

            // Configurer la fenêtre
            primaryStage.setTitle(TITLE);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false); // Taille fixe pour un jeu
            primaryStage.show();

            // Centrer la fenêtre
            primaryStage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'application : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}