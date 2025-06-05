package com.bomberman.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
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
import java.util.Optional;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

    // FXML Elements
    @FXML private StackPane root;
    @FXML private VBox menuContainer;
    @FXML private VBox menuButtons;
    @FXML private ImageView titleImage;
    @FXML private Label gameTitle;
    @FXML private Label gameSubtitle;
    @FXML private Label versionLabel;
    @FXML private Label controlsLabel;

    // Button containers
    @FXML private HBox playButtonContainer;
    @FXML private HBox settingsButtonContainer;
    @FXML private HBox creditsButtonContainer;
    @FXML private HBox exitButtonContainer;

    // Buttons
    @FXML private Button playButton;
    @FXML private Button settingsButton;
    @FXML private Button creditsButton;
    @FXML private Button exitButton;

    // Cursors
    @FXML private Label playCursor;
    @FXML private Label settingsCursor;
    @FXML private Label creditsCursor;
    @FXML private Label exitCursor;

    // Navigation state
    private int selectedIndex = 0;
    private List<MenuOption> menuOptions;
    private Timeline cursorBlinkAnimation;
    private boolean isAnimationRunning = false;

    // Menu option class to hold button and cursor references
    private static class MenuOption {
        final Button button;
        final Label cursor;
        final HBox container;

        MenuOption(Button button, Label cursor, HBox container) {
            this.button = button;
            this.cursor = cursor;
            this.container = container;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMenuOptions();
        setupKeyboardNavigation();
        setupCursorAnimation();
        setupImageFallback();

        // Set initial selection
        updateSelection();

        // Request focus for keyboard navigation
        Platform.runLater(() -> {
            root.requestFocus();
        });
    }

    private void setupMenuOptions() {
        menuOptions = new ArrayList<>();
        menuOptions.add(new MenuOption(playButton, playCursor, playButtonContainer));
        menuOptions.add(new MenuOption(settingsButton, settingsCursor, settingsButtonContainer));
        menuOptions.add(new MenuOption(creditsButton, creditsCursor, creditsButtonContainer));
        menuOptions.add(new MenuOption(exitButton, exitCursor, exitButtonContainer));
    }

    private void setupKeyboardNavigation() {
        root.setOnKeyPressed(this::handleKeyPressed);
        root.setFocusTraversable(true);

        // Ensure root maintains focus
        root.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                Platform.runLater(() -> root.requestFocus());
            }
        });
    }

    private void setupCursorAnimation() {
        // Create blinking animation for the selected cursor
        cursorBlinkAnimation = new Timeline(
                new KeyFrame(Duration.seconds(0.5), e -> {
                    MenuOption selected = menuOptions.get(selectedIndex);
                    selected.cursor.setVisible(!selected.cursor.isVisible());
                })
        );
        cursorBlinkAnimation.setCycleCount(Animation.INDEFINITE);
        cursorBlinkAnimation.play();
        isAnimationRunning = true;
    }

    private void setupImageFallback() {
        // Show text title if image fails to load
        titleImage.imageProperty().addListener((obs, oldImage, newImage) -> {
            if (newImage == null || newImage.isError()) {
                titleImage.setVisible(false);
                gameTitle.setVisible(true);
            }
        });
    }

    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();

        switch (code) {
            case UP:
            case W:
                navigateUp();
                event.consume();
                break;
            case DOWN:
            case S:
                navigateDown();
                event.consume();
                break;
            case ENTER:
            case SPACE:
                activateSelected();
                event.consume();
                break;
            case ESCAPE:
                handleExitButton();
                event.consume();
                break;
        }
    }

    private void navigateUp() {
        if (selectedIndex > 0) {
            selectedIndex--;
            updateSelection();
            playNavigationSound();
        }
    }

    private void navigateDown() {
        if (selectedIndex < menuOptions.size() - 1) {
            selectedIndex++;
            updateSelection();
            playNavigationSound();
        }
    }

    private void updateSelection() {
        // Hide all cursors and remove selection style
        for (int i = 0; i < menuOptions.size(); i++) {
            MenuOption option = menuOptions.get(i);
            option.cursor.setVisible(false);
            option.button.getStyleClass().remove("selected");
        }

        // Show selected cursor and add selection style
        MenuOption selected = menuOptions.get(selectedIndex);
        selected.cursor.setVisible(true);
        if (!selected.button.getStyleClass().contains("selected")) {
            selected.button.getStyleClass().add("selected");
        }
    }

    private void activateSelected() {
        MenuOption selected = menuOptions.get(selectedIndex);
        playSelectionSound();

        // Trigger the button's action
        if (selected.button == playButton) {
            handlePlayButton();
        } else if (selected.button == settingsButton) {
            handleSettingsButton();
        } else if (selected.button == creditsButton) {
            handleCreditsButton();
        } else if (selected.button == exitButton) {
            handleExitButton();
        }
    }

    // Button action handlers
    @FXML
    private void handlePlayButton() {
        System.out.println("Starting game...");

        try {
            // Load the game scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
            Parent gameRoot = loader.load();

            Scene gameScene = new Scene(gameRoot, 800, 600);
            gameScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) playButton.getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - Game");

            // Stop menu animations
            shutdown();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Game Error", "Could not load the game scene.",
                    "Please check that game.fxml exists in the resources/fxml folder.");
        }
    }


    @FXML
    private void handleSettingsButton() {
        System.out.println("Opening settings...");

        Alert alert = createStyledAlert("Settings",
                "Game Settings",
                "Resolution: 800x600\n" +
                        "Sound: Enabled\n" +
                        "Controls: Arrow Keys + Space\n" +
                        "Difficulty: Normal\n\n" +
                        "Settings configuration coming soon!");
        alert.showAndWait();
    }

    @FXML
    private void handleCreditsButton() {
        System.out.println("Showing credits...");

        Alert alert = createStyledAlert("Credits",
                "Super Bomberman - Retro Edition",
                "Developed with JavaFX\n\n" +
                        "Programming: Your Team\n" +
                        "Graphics: Retro Style\n" +
                        "Music: 8-bit Inspired\n" +
                        "Font: Press Start 2P\n\n" +
                        "Thank you for playing!");
        alert.showAndWait();
    }

    @FXML
    private void handleExitButton() {
        System.out.println("Exiting game...");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Game");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("All unsaved progress will be lost.");

        // Apply custom styling
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/menu.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("alert");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            shutdown();
            Platform.exit();
        }
    }

    private Alert createStyledAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Apply custom styling
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/menu.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("alert");

        return alert;
    }

    private void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/menu.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("alert");

        alert.showAndWait();
    }

    // Sound effect placeholders (implement with actual sound library if desired)
    private void playNavigationSound() {
        // Placeholder for navigation sound effect
        // Could implement with JavaFX MediaPlayer or other audio library
        System.out.println("♪ Navigation sound");
    }

    private void playSelectionSound() {
        // Placeholder for selection sound effect
        System.out.println("♪ Selection sound");
    }

    // Public methods for external control
    public void selectOption(int index) {
        if (index >= 0 && index < menuOptions.size()) {
            selectedIndex = index;
            updateSelection();
        }
    }

    public void shutdown() {
        if (cursorBlinkAnimation != null && isAnimationRunning) {
            cursorBlinkAnimation.stop();
            isAnimationRunning = false;
        }
    }

    // Getters for testing or external access
    public int getSelectedIndex() {
        return selectedIndex;
    }

    public int getMenuOptionsCount() {
        return menuOptions.size();
    }
}