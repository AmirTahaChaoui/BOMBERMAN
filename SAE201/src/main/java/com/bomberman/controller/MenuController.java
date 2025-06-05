package com.bomberman.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

    // Éléments FXML
    @FXML private VBox menuContainer;
    @FXML private Label gameTitle;
    @FXML private Label gameSubtitle;
    @FXML private VBox menuButtons;

    // Boutons
    @FXML private Button playButton;
    @FXML private Button settingsButton;
    @FXML private Button moreButton;

    // Curseurs
    @FXML private Label playCursor;
    @FXML private Label settingsCursor;
    @FXML private Label moreCursor;

    // Containers des boutons
    @FXML private HBox playButtonContainer;
    @FXML private HBox settingsButtonContainer;
    @FXML private HBox moreButtonContainer;

    @FXML private Label versionLabel;

    // Variables pour la navigation
    private int selectedIndex = 0;
    private List<Button> buttons;
    private List<Label> cursors;
    private Timeline cursorBlinkTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les listes pour la navigation
        buttons = new ArrayList<>();
        buttons.add(playButton);
        buttons.add(settingsButton);
        buttons.add(moreButton);

        cursors = new ArrayList<>();
        cursors.add(playCursor);
        cursors.add(settingsCursor);
        cursors.add(moreCursor);

        // Configurer la navigation au clavier
        setupKeyboardNavigation();

        // Démarrer l'animation du curseur clignotant
        startCursorBlinking();

        // Initialiser le son (optionnel)
        initializeSound();

        // Configurer les événements hover
        setupHoverEvents();

        // Sélectionner le premier bouton par défaut
        updateSelection();
    }

    private void setupKeyboardNavigation() {
        // Assurer que le container peut recevoir les événements clavier
        menuContainer.setFocusTraversable(true);
        menuContainer.requestFocus();

        menuContainer.setOnKeyPressed(this::handleKeyPressed);
    }

    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();

        switch (code) {
            case UP:
                selectedIndex = (selectedIndex - 1 + buttons.size()) % buttons.size();
                updateSelection();
                event.consume();
                break;

            case DOWN:
                selectedIndex = (selectedIndex + 1) % buttons.size();
                updateSelection();
                event.consume();
                break;

            case ENTER:
            case SPACE:
                buttons.get(selectedIndex).fire();
                event.consume();
                break;
        }
    }

    private void updateSelection() {
        // Cacher tous les curseurs et enlever la classe selected
        for (int i = 0; i < buttons.size(); i++) {
            cursors.get(i).setVisible(false);
            buttons.get(i).getStyleClass().remove("selected");
        }

        // Afficher le curseur du bouton sélectionné et ajouter la classe selected
        cursors.get(selectedIndex).setVisible(true);
        if (!buttons.get(selectedIndex).getStyleClass().contains("selected")) {
            buttons.get(selectedIndex).getStyleClass().add("selected");
        }
    }

    private void startCursorBlinking() {
        cursorBlinkTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), e -> {
                    Label currentCursor = cursors.get(selectedIndex);
                    currentCursor.setVisible(!currentCursor.isVisible());
                })
        );
        cursorBlinkTimeline.setCycleCount(Timeline.INDEFINITE);
        cursorBlinkTimeline.play();
    }

    private void setupHoverEvents() {
        for (int i = 0; i < buttons.size(); i++) {
            final int index = i;
            buttons.get(i).setOnMouseEntered(e -> {
                selectedIndex = index;
                updateSelection();
            });
        }
    }

    private void initializeSound() {
        try {
            // Vous pouvez ajouter un fichier son .wav dans resources/sounds/
            // beepSound = new AudioClip(getClass().getResource("/sounds/beep.wav").toString());
        } catch (Exception e) {
            System.out.println("Son non trouvé, utilisation du son système");
        }
    }


    // Gestionnaires d'événements pour les boutons
    @FXML
    private void handlePlayButton() {
        System.out.println("PLAY sélectionné");

        // Arrêter l'animation du curseur
        if (cursorBlinkTimeline != null) {
            cursorBlinkTimeline.stop();
        }

        loadGameScene();
    }

    @FXML
    private void handleSettingsButton() {
        System.out.println("SETTINGS sélectionné");

        showSettingsDialog();
    }

    @FXML
    private void handleMoreButton() {
        System.out.println("MORE sélectionné");


        // Ici vous pouvez afficher les crédits, aide, etc.
        showMoreDialog();
    }

    private void loadGameScene() {
        try {
            // Charger la scène du jeu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
            Scene gameScene = new Scene(loader.load());

            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) menuContainer.getScene().getWindow();

            // Charger le CSS du jeu si nécessaire
            gameScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            // Changer de scène
            stage.setScene(gameScene);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement du jeu : " + e.getMessage());

            // Fallback : afficher un message
            System.out.println("Démarrage du jeu... (game.fxml non trouvé)");
        }
    }

    private void showSettingsDialog() {
        // Pour l'instant, juste un message
        System.out.println("Menu SETTINGS - À implémenter");

        // Vous pouvez créer un nouveau Stage pour les paramètres
        // ou utiliser une Alert/Dialog
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Settings");
        alert.setHeaderText("Paramètres du jeu");
        alert.setContentText("Menu des paramètres à implémenter...");
        alert.showAndWait();
    }

    private void showMoreDialog() {
        // Pour l'instant, juste un message
        System.out.println("Menu MORE - À implémenter");

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("More");
        alert.setHeaderText("Plus d'options");
        alert.setContentText("Crédits, aide, etc. - À implémenter...");
        alert.showAndWait();
    }

    // Méthode pour arrêter proprement les animations
    public void shutdown() {
        if (cursorBlinkTimeline != null) {
            cursorBlinkTimeline.stop();
        }
    }
}