package com.bomberman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.text.Font;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Charger la police personnalisée
        loadCustomFont();

        try {
            // Charger le FXML du menu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            StackPane root = loader.load();

            // Créer la scène
            Scene scene = new Scene(root, 800, 600);

            // Charger le CSS
            scene.getStylesheets().add(getClass().getResource("/css/menu.css").toExternalForm());

            // Configurer la fenêtre
            primaryStage.setTitle("Super Bomberman");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            // Gérer la fermeture propre
            primaryStage.setOnCloseRequest(event -> {
                // Arrêter les animations du contrôleur si nécessaire
                try {
                    com.bomberman.controller.MenuController controller = loader.getController();
                    if (controller != null) {
                        controller.shutdown();
                    }
                } catch (Exception e) {
                    System.out.println("Erreur lors de l'arrêt : " + e.getMessage());
                }
            });

            // Afficher la fenêtre
            primaryStage.show();

            // S'assurer que le focus est sur la racine pour la navigation clavier
            root.requestFocus();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement du menu : " + e.getMessage());
        }
    }

    private void loadCustomFont() {
        try {
            // Charger la police Press Start 2P
            Font pressStart2P = Font.loadFont(
                    getClass().getResourceAsStream("/fonts/PressStart2P.ttf"),
                    12
            );

            if (pressStart2P != null) {
                System.out.println("Police Press Start 2P chargée avec succès");
            } else {
                System.out.println("Impossible de charger la police Press Start 2P, utilisation de la police par défaut");
            }

        } catch (Exception e) {
            System.out.println("Erreur lors du chargement de la police : " + e.getMessage());
            System.out.println("Utilisation de la police par défaut");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}