package com.bomberman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import java.net.URL;

public class    Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Charger la police personnalisée
        loadCustomFont();

        try {
            // Debug: Afficher les ressources disponibles
            debugResources();

            // Charger le FXML du menu
            URL fxmlUrl = getClass().getResource("/fxml/menu.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("Cannot find menu.fxml in /fxml/");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            StackPane root = loader.load();

            // Créer la scène
            Scene scene = new Scene(root, 800, 600);

            // Charger le CSS avec gestion d'erreur
            loadStylesheet(scene, "/css/menu.css");

            // Configurer la fenêtre
            primaryStage.setTitle("Super Bomberman");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            // Gérer la fermeture propre    
            primaryStage.setOnCloseRequest(event -> {
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
            // Essayer de continuer avec le style par défaut
            createFallbackMenu(primaryStage);
        }
    }

    private void loadStylesheet(Scene scene, String cssPath) {
        try {
            URL cssUrl = getClass().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS chargé avec succès : " + cssPath);
            } else {
                System.err.println("ATTENTION: CSS non trouvé : " + cssPath);
                System.err.println("Le menu utilisera le style par défaut.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du CSS : " + e.getMessage());
        }
    }

    private void debugResources() {
        System.out.println("=== DEBUG RESSOURCES ===");

        // Vérifier les ressources principales
        String[] resources = {
                "/fxml/menu.fxml",
                "/css/menu.css",
                "/fonts/PressStart2P.ttf",
                "/images/logo.png"
        };

        for (String resource : resources) {
            URL url = getClass().getResource(resource);
            if (url != null) {
                System.out.println("✓ Trouvé: " + resource + " -> " + url);
            } else {
                System.out.println("✗ Manquant: " + resource);
            }
        }
        System.out.println("========================");
    }

    private void loadCustomFont() {
        try {
            // Charger la police Press Start 2P
            URL fontUrl = getClass().getResource("/fonts/PressStart2P.ttf");
            if (fontUrl != null) {
                Font pressStart2P = Font.loadFont(fontUrl.openStream(), 12);
                if (pressStart2P != null) {
                    System.out.println("Police Press Start 2P chargée avec succès");
                } else {
                    System.out.println("Échec du chargement de la police Press Start 2P");
                }
            } else {
                System.out.println("Fichier de police Press Start 2P non trouvé");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement de la police : " + e.getMessage());
            System.out.println("Utilisation de la police par défaut");
        }
    }

    private void createFallbackMenu(Stage primaryStage) {
        try {
            System.out.println("Création d'un menu de secours...");

            // Créer un menu simple en cas d'échec
            StackPane fallbackRoot = new StackPane();
            javafx.scene.control.Label label = new javafx.scene.control.Label("Super Bomberman\n\nErreur de chargement des ressources");
            label.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-text-alignment: center;");
            fallbackRoot.getChildren().add(label);
            fallbackRoot.setStyle("-fx-background-color: #1a1a1a;");

            Scene fallbackScene = new Scene(fallbackRoot, 800, 600);
            primaryStage.setTitle("Super Bomberman - Mode de secours");
            primaryStage.setScene(fallbackScene);
            primaryStage.show();

        } catch (Exception fallbackError) {
            System.err.println("Impossible de créer le menu de secours : " + fallbackError.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}