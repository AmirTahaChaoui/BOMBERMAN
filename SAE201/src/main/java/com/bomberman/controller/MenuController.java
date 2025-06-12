package com.bomberman.controller;

import com.bomberman.model.User;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.stream.Collectors;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MenuController implements Initializable {

    // FXML Elements existants
    @FXML private StackPane root;
    @FXML private ImageView titleImage;
    @FXML private Label gameTitle;

    // Button containers existants
    @FXML private HBox playButtonContainer;
    @FXML private HBox settingsButtonContainer;
    @FXML private HBox loginButtonContainer;
    @FXML private HBox exitButtonContainer;
    @FXML private HBox rankingButtonContainer;

    // Buttons existants
    @FXML private Button playButton;
    @FXML private Button settingsButton;
    @FXML private Button loginButton;
    @FXML private Button exitButton;
    @FXML private Button rankingButton;

    // Cursors existants
    @FXML private Label playCursor;
    @FXML private Label settingsCursor;
    @FXML private Label loginCursor;
    @FXML private Label exitCursor;
    @FXML private Label rankingCursor;

    // NOUVEAUX ÉLÉMENTS - Système de connexion
    @FXML private StackPane loginView;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label loginErrorLabel;

    // NOUVEAUX ÉLÉMENTS - Système d'inscription
    @FXML private StackPane registerView;
    @FXML private VBox registerContent;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField registerUsernameField;
    @FXML private PasswordField registerPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label registerErrorLabel;

    // Navigation state existante
    private int selectedIndex = 0;
    private List<MenuOption> menuOptions;
    private Timeline cursorBlinkAnimation;
    private boolean isAnimationRunning = false;

    // NOUVEAU : Gestion des utilisateurs
    private UserManager userManager;

    // Variables pour la vue thème
    @FXML private StackPane themeView;
    @FXML private Button theme1Button;
    @FXML private Button theme2Button;
    @FXML private VBox mapButtonsContainer;

    private List<Button> mapButtons = new ArrayList<>();

    private MapManager mapManager;
    private static String selectedMapName = "Map Classique"; // Map par défaut

    @FXML private StackPane gameModeView;
    @FXML private Button normalModeButton;


    // Vue du classement
    @FXML private StackPane rankingView;

    // État de navigation
    private boolean isInSubMenu = false;


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

    // Gestionnaire de musique
    private MusicManager musicManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le gestionnaire d'utilisateurs
        userManager = UserManager.getInstance();

        setupMenuOptions();
        setupKeyboardNavigation();
        setupCursorAnimation();
        setupImageFallback();
        setupMusic();

        //Configurer le système de connexion
        setupLoginSystem();

        updateSelection();

        Platform.runLater(() -> {
            root.requestFocus();
        });

        mapManager = MapManager.getInstance();
    }

    // NOUVELLE MÉTHODE : Configuration du système de connexion
    private void setupLoginSystem() {
        // Mettre à jour l'affichage selon l'état de connexion
        updateLoginDisplay();

        // Cacher les vues de connexion et inscription par défaut
        if (loginView != null) {
            loginView.setVisible(false);
        }
        if (registerView != null) {
            registerView.setVisible(false);
        }
        if (themeView != null) {
            themeView.setVisible(false);
        }
        if (gameModeView != null) {  // ← NOUVEAU
            gameModeView.setVisible(false);
        }
    }

    // NOUVELLE MÉTHODE : Mettre à jour l'affichage de connexion
    private void updateLoginDisplay() {
        if (userManager.isLoggedIn()) {
            User currentUser = userManager.getCurrentUser();
            loginButton.setText("COMPTE");  // Changé de loginButton à creditsButton
            // userInfoLabel supprimé
        } else {
            loginButton.setText("SE CONNECTER");  // Changé de loginButton à creditsButton
            // userInfoLabel supprimé
        }
    }


    @FXML
    private void handleConnectButton() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Vider le message d'erreur
        loginErrorLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            showLoginError("Veuillez remplir tous les champs");
            return;
        }

        // Tentative de connexion
        boolean success = userManager.login(username, password);

        if (success) {
            hideLoginView();
            updateLoginDisplay();
            clearLoginFields();
        } else {
            showLoginError("Nom d'utilisateur ou mot de passe incorrect");
        }
    }

    @FXML
    private void handleCancelButton() {
        hideLoginView();
        clearLoginFields();
        loginErrorLabel.setVisible(false);
    }

    @FXML
    private void handleCreateAccountLink() {
        showRegisterView();
    }

    // NOUVELLES MÉTHODES : Gestion de l'inscription
    @FXML
    private void handleCreateAccountButton() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = registerUsernameField.getText().trim();
        String password = registerPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Vider le message d'erreur
        registerErrorLabel.setVisible(false);


        // Vérifier que les mots de passe correspondent
        if (!password.equals(confirmPassword)) {
            showRegisterError("Les mots de passe ne correspondent pas");
            return;
        }

        // Vérifier la longueur du mot de passe
        if (password.length() < 4) {
            showRegisterError("Le mot de passe doit contenir au moins 4 caractères");
            return;
        }

        // Tentative de création du compte
        boolean success = userManager.createUser(username, password, firstName, lastName);

        if (success) {
            // Connexion automatique après création
            boolean loginSuccess = userManager.login(username, password);

            hideRegisterView();
            updateLoginDisplay();
            clearRegisterFields();

            // Afficher un message de succès
            Alert alert = createStyledAlert("Compte créé",
                    "Bienvenue " + firstName + " !",
                    "Votre compte a été créé avec succès.\nVous êtes maintenant connecté(e) !");
            alert.showAndWait();

        } else {
            showRegisterError("Ce nom d'utilisateur existe déjà");
        }
    }

    @FXML
    private void handleCancelRegisterButton() {
        hideRegisterView();
        clearRegisterFields();
        registerErrorLabel.setVisible(false);
    }

    @FXML
    private void handleBackToLoginLink() {
        hideRegisterView();
        showLoginView();
        clearRegisterFields();
    }

    // NOUVELLES MÉTHODES : Gestion de l'affichage
    private void showLoginView() {
        hideRegisterView();
        hideRankingView();
        loginView.setVisible(true);
        loginView.toFront();
        // Désactiver la navigation clavier du menu
        root.setFocusTraversable(false);
        Platform.runLater(() -> {
            usernameField.requestFocus();
        });
    }

    private void hideLoginView() {
        loginView.setVisible(false);
        // Réactiver la navigation clavier du menu
        root.setFocusTraversable(true);
        Platform.runLater(() -> {
            root.requestFocus();
        });
    }

    private void showRegisterView() {
        hideLoginView();
        hideRankingView();
        registerView.setVisible(true);
        registerView.toFront();
        // Désactiver la navigation clavier du menu
        root.setFocusTraversable(false);
        Platform.runLater(() -> {
            firstNameField.requestFocus();
        });
    }

    private void hideRegisterView() {
        registerView.setVisible(false);
        // Réactiver la navigation clavier du menu
        root.setFocusTraversable(true);
        Platform.runLater(() -> {
            root.requestFocus();
        });
    }

    private void clearLoginFields() {
        usernameField.clear();
        passwordField.clear();
    }

    private void clearRegisterFields() {
        firstNameField.clear();
        lastNameField.clear();
        registerUsernameField.clear();
        registerPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void showLoginError(String message) {
        loginErrorLabel.setText(message);
        loginErrorLabel.setVisible(true);
    }

    private void showRegisterError(String message) {
        registerErrorLabel.setText(message);
        registerErrorLabel.setVisible(true);
    }

    private void showUserProfile() {
        User currentUser = userManager.getCurrentUser();

        // Créer un dialog avec les infos utilisateur
        Alert alert = createStyledAlert("Profil Utilisateur",
                "Informations du compte",
                String.format("Nom: %s %s\n" +
                                "Nom d'utilisateur: %s\n" +
                                "Parties jouées: %d\n" +
                                "Parties gagnées: %d\n" +
                                "Ratio victoires: %.1f%%\n" +
                                "Dernière connexion: %s",
                        currentUser.getFirstName(),
                        currentUser.getLastName(),
                        currentUser.getUsername(),
                        currentUser.getGamesPlayed(),
                        currentUser.getGamesWon(),
                        currentUser.getWinRate(),
                        currentUser.getLastLoginDate()));

        // Ajouter un bouton de déconnexion
        ButtonType logoutButton = new ButtonType("Se déconnecter");
        alert.getButtonTypes().add(logoutButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == logoutButton) {
            userManager.logout();
            updateLoginDisplay();
        }
    }

    // Méthodes existantes (inchangées)
    private void setupMusic() {
        musicManager = MusicManager.getInstance();
        musicManager.setVolume(0.3);
        musicManager.startBackgroundMusic();
    }

    private void setupMenuOptions() {
        menuOptions = new ArrayList<>();
        menuOptions.add(new MenuOption(playButton, playCursor, playButtonContainer));
        menuOptions.add(new MenuOption(settingsButton, settingsCursor, settingsButtonContainer));
        menuOptions.add(new MenuOption(loginButton, loginCursor, loginButtonContainer));
        menuOptions.add(new MenuOption(exitButton, exitCursor, exitButtonContainer));
        menuOptions.add(new MenuOption(rankingButton, rankingCursor, rankingButtonContainer)); // NOUVEAU

    }

    /* mettre à jour l'affichage du menu*/
    private void updateMenuDisplay() {
        if (isInSubMenu) {
            // Affichage du sous-menu
            playButton.setText("LANCER PARTIE");
            settingsButton.setText("THEMES");
            loginButton.setText("MAP EDITOR");
            exitButton.setText("RETOUR");
        } else {
            // Affichage du menu principal
            playButton.setText("JOUER");
            settingsButton.setText("PARAMETRES");
            loginButton.setText("SE CONNECTER");
            exitButton.setText("QUITTER");
        }
    }

    private void setupKeyboardNavigation() {
        root.setOnKeyPressed(this::handleKeyPressed);
        root.setFocusTraversable(true);

        // Ensure root maintains focus SEULEMENT si aucune vue n'est ouverte
        root.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !isAnyViewVisible()) {
                Platform.runLater(() -> root.requestFocus());
            }
        });
    }

    // NOUVELLE MÉTHODE : Vérifier si une vue est visible
    private boolean isAnyViewVisible() {
        return (loginView != null && loginView.isVisible()) ||
                (registerView != null && registerView.isVisible()) ||
                (themeView != null && themeView.isVisible()) ||
                (gameModeView != null && gameModeView.isVisible());
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
        // Ne pas traiter les touches si une vue est visible
        if ((loginView != null && loginView.isVisible()) ||
                (registerView != null && registerView.isVisible()) ||
                (themeView != null && themeView.isVisible()) ||
                (gameModeView != null && gameModeView.isVisible())) { // ← NOUVEAU

            if (event.getCode() == KeyCode.ESCAPE) {
                if (loginView.isVisible()) {
                    handleCancelButton();
                } else if (registerView.isVisible()) {
                    handleCancelRegisterButton();
                } else if (themeView.isVisible()) {
                    handleThemeCloseButton();
                } else if (gameModeView.isVisible()) { // ← NOUVEAU
                    handleGameModeBackButton();
                }
                event.consume();
            }
            return;
        }

        // Le reste du code pour la navigation du menu principal...
        KeyCode code = event.getCode();
        switch (code) {
            case UP:
            case Z:
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
            case B:
                toggleMusic();
                event.consume();
                break;
            case N:
                musicManager.nextTrack();
                event.consume();
                break;
            case P:
                musicManager.previousTrack();
                event.consume();
                break;
        }
    }

    private void toggleMusic() {
        if (musicManager.isPlaying()) {
            musicManager.pauseBackgroundMusic();
        } else {
            musicManager.resumeBackgroundMusic();
        }
    }

    // Méthode générique pour jouer un son (effets sonores)
    private void playSound(String soundFileName) {
        URL soundUrl = getClass().getResource("/Sound/" + soundFileName);
        if (soundUrl == null) {
            return;
        }

        String musicFile = soundUrl.toExternalForm();
        Media media = new Media(musicFile);
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(0.7);
        mediaPlayer.play();
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
        } else if (selected.button == rankingButton) { // NOUVEAU
            handleRankingButton();
        } else if (selected.button == loginButton) {
            handleLoginButton();
        } else if (selected.button == exitButton) {
            handleExitButton();
        }
    }

    // Button action handlers (inchangés)
    @FXML
    private void handlePlayButton() {
        if (isInSubMenu) {
            // Dans le sous-menu : "LANCER PARTIE"
            startGame(); // Utiliser la méthode existante
        } else {
            // Dans le menu principal : "JOUER" -> aller au sous-menu
            isInSubMenu = true;
            updateMenuDisplay();

            // Remettre la sélection sur le premier élément
            selectedIndex = 0;
            updateSelection();
        }
    }

    private void startGame() {
        // NOUVEAU : Ouvrir la vue de sélection de mode au lieu de lancer directement
        showGameModeView();
    }

    private void showGameModeView() {
        hideLoginView();
        hideRegisterView();
        hideThemeView();

        gameModeView.setVisible(true);
        gameModeView.toFront();

        // Désactiver la navigation clavier du menu
        root.setFocusTraversable(false);

        Platform.runLater(() -> {
            normalModeButton.requestFocus();
        });
    }

    private void hideGameModeView() {
        gameModeView.setVisible(false);

        // Réactiver la navigation clavier du menu
        root.setFocusTraversable(true);

        Platform.runLater(() -> {
            root.requestFocus();
        });
    }

    @FXML
    private void handleNormalModeButton() {
        hideGameModeView();
        launchNormalMode();
    }

    @FXML
    private void handleCaptureFlagModeButton() {
        hideGameModeView();
        launchCaptureTheFlagMode();
    }

    @FXML
    private void handleGameModeBackButton() {
        hideGameModeView();
    }

    // ========== MÉTHODES DE LANCEMENT DES MODES ==========

    private void launchNormalMode() {
        try {
            // Passer la map sélectionnée au GameController
            GameControllerTheme1.setSelectedMap(selectedMapName);

            // Sauvegarder les dimensions actuelles du menu
            Stage stage = (Stage) playButton.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();

            // Passer les dimensions au GameController
            GameControllerTheme1.setOriginalMenuDimensions(currentWidth, currentHeight);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/theme1.fxml"));
            Parent gameRoot = loader.load();

            // Créer la scène de jeu
            Scene gameScene = new Scene(gameRoot, 800, 800);
            gameScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - Mode Normal");

            // Forcer les dimensions du jeu
            stage.setWidth(800);
            stage.setHeight(800);
            stage.centerOnScreen();

            shutdown();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Erreur", "Impossible de charger le mode normal",
                    "Vérifiez que le fichier theme1.fxml existe dans resources/fxml/");
        }
    }

    private void launchCaptureTheFlagMode() {
        try {
            // Passer les données nécessaires au CaptureTheFlagController
            CaptureTheFlagController.setSelectedMap(selectedMapName);
            CaptureTheFlagController.setCurrentTheme(GameControllerTheme1.getCurrentTheme());

            // Sauvegarder les dimensions actuelles du menu
            Stage stage = (Stage) playButton.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();

            CaptureTheFlagController.setOriginalMenuDimensions(currentWidth, currentHeight);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/theme1.fxml"));
            Parent gameRoot = loader.load();

            // Créer la scène de jeu
            Scene gameScene = new Scene(gameRoot, 800, 800);
            gameScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - Capture du Drapeau");
            stage.setWidth(800);
            stage.setHeight(800);
            stage.centerOnScreen();

            shutdown();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void handleSettingsButton() {
        if (isInSubMenu) {
            // Dans le sous-menu : "THEMES"
            handleThemeSelection();
        } else {
            // Dans le menu principal : "PARAMETRE" -> afficher les paramètres
            String musicInfo = String.format("Musique: %s (Vol: %.0f%%)\nPiste actuelle: %s",
                    musicManager.isPlaying() ? "Activée" : "Désactivée",
                    musicManager.getVolume() * 100,
                    musicManager.getCurrentTrackName());

            Alert alert = createStyledAlert("Paramètres",
                    "Paramètres du jeu",
                    "Résolution: 800x600\n" +
                            musicInfo + "\n" +
                            "Contrôles: Flèches + Entrée\n" +
                            "Difficulté: Normal\n" +
                            "Contrôles musique:\n" +
                            "B = Pause/Reprise\n" +
                            "N = Piste suivante\n" +
                            "P = Piste précédente\n\n" +
                            "Paramètres de configuration prochainement!");
            alert.showAndWait();
        }
    }

    // NOUVELLES MÉTHODES : Gestionnaires de connexion
    @FXML
    private void handleLoginButton() {
        if (isInSubMenu) {
            // Dans le sous-menu : "MAP EDITOR"
            handleMapEditor();
        } else {
            // Dans le menu principal : "SE CONNECTER" -> système de connexion
            if (userManager.isLoggedIn()) {
                showUserProfile();
            } else {
                showLoginView();
            }
        }
    }

    @FXML
    private void handleExitButton() {
        if (isInSubMenu) {
            // Dans le sous-menu : "RETOUR" -> retour au menu principal
            isInSubMenu = false;
            updateMenuDisplay();

            // Remettre la sélection sur "JOUER" (index 0)
            selectedIndex = 0;
            updateSelection();
        } else {
            // Dans le menu principal : "QUITTER" -> fermer le jeu

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Fermeture du jeu");
            alert.setHeaderText("Êtes vous sûr de vouloir quitter ?");
            alert.setContentText("Tous les progrès non sauvegardés seront perdus.");

            alert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/css/menu.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("alert");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                shutdown();
                musicManager.shutdown();
                Platform.exit();
            }
        }
    }

    // NOUVELLE MÉTHODE : Afficher le classement
    @FXML
    private void handleRankingButton() {
        showRankingView();
    }

    private void showRankingView() {
        hideLoginView();
        hideRegisterView();

        // Récupérer tous les utilisateurs et créer le classement
        List<User> allUsers = userManager.getAllUsers();

        if (allUsers.isEmpty()) {
            Alert alert = createStyledAlert("Classement",
                    "Aucun joueur",
                    "Aucun joueur n'est encore inscrit !");
            alert.showAndWait();
            return;
        }

        // Trier les utilisateurs par nombre de victoires (décroissant), puis par ratio de victoires
        List<User> rankedUsers = allUsers.stream()
                .sorted((u1, u2) -> {
                    // D'abord par nombre de victoires
                    int winsComparison = Integer.compare(u2.getGamesWon(), u1.getGamesWon());
                    if (winsComparison != 0) {
                        return winsComparison;
                    }
                    // Ensuite par ratio de victoires
                    return Double.compare(u2.getWinRate(), u1.getWinRate());
                })
                .collect(Collectors.toList());

        // Construire le texte du classement
        StringBuilder rankingText = new StringBuilder();
        rankingText.append("🏆 CLASSEMENT DES JOUEURS 🏆\n\n");

        for (int i = 0; i < rankedUsers.size(); i++) {
            User user = rankedUsers.get(i);
            String medal = "";

            // Ajouter la position des joueurs

            medal = " Top " + (i + 1) + " : "; // Affichage du numéro dans le classement

            rankingText.append(String.format("%s%s %s\n",
                    medal,
                    user.getFirstName(),
                    user.getLastName()));

            rankingText.append(String.format("   Victoires: %d | Parties: %d | Ratio: %.1f%%\n",
                    user.getGamesWon(),
                    user.getGamesPlayed(),
                    user.getWinRate()));

            if (i < rankedUsers.size() - 1) {
                rankingText.append("\n");
            }
        }

        // Afficher le classement dans une alerte stylée
        Alert rankingAlert = createStyledAlert("Classement",
                "Tableau des scores",
                rankingText.toString());

        // Rendre l'alerte plus large pour un meilleur affichage
        rankingAlert.getDialogPane().setPrefWidth(500);
        rankingAlert.showAndWait();
    }

    private void hideRankingView() {
        if (rankingView != null) {
            rankingView.setVisible(false);
        }
        // Réactiver la navigation clavier du menu
        root.setFocusTraversable(true);
        Platform.runLater(() -> {
            root.requestFocus();
        });
    }

    @FXML
    private void handleCloseRankingButton() {
        hideRankingView();
    }




    private void handleThemeSelection() {
        showThemeView();
    }

    private void showThemeView() {
        hideLoginView();
        hideRegisterView();

        // Charger les maps disponibles
        loadAvailableMaps();

        themeView.setVisible(true);
        themeView.toFront();

        // IMPORTANT : Désactiver la navigation clavier du menu
        root.setFocusTraversable(false);

        updateThemeButtons();

        Platform.runLater(() -> {
            theme1Button.requestFocus();
        });
    }

    private void hideThemeView() {
        themeView.setVisible(false);

        // IMPORTANT : Réactiver la navigation clavier du menu
        root.setFocusTraversable(true);

        Platform.runLater(() -> {
            root.requestFocus();
        });
    }

    private void loadAvailableMaps() {
        if (mapButtonsContainer != null) {
            // Vider les boutons existants
            mapButtonsContainer.getChildren().clear();
            mapButtons.clear();

            List<String> availableMaps = mapManager.getMapsList();

            if (availableMaps.isEmpty()) {
                Label noMapsLabel = new Label("Aucune map disponible");
                noMapsLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 9px;");
                mapButtonsContainer.getChildren().add(noMapsLabel);
            } else {
                // Créer un bouton pour chaque map
                for (String mapName : availableMaps) {
                    Button mapButton = new Button(mapName);
                    mapButton.getStyleClass().add("theme-btn");
                    mapButton.setMaxWidth(280);
                    mapButton.setMinWidth(280);

                    // Marquer la map sélectionnée
                    if (mapName.equals(selectedMapName)) {
                        mapButton.getStyleClass().add("selected");
                    }

                    // Gestionnaire de clic
                    mapButton.setOnAction(e -> selectMap(mapName, mapButton));

                    mapButtons.add(mapButton);
                    mapButtonsContainer.getChildren().add(mapButton);
                }
            }
        }
    }

    private void selectMap(String mapName, Button clickedButton) {
        // Changer la sélection
        selectedMapName = mapName;

        // Mettre à jour l'apparence des boutons
        updateMapButtonsSelection();
    }

    private void updateMapButtonsSelection() {
        for (Button mapButton : mapButtons) {
            mapButton.getStyleClass().removeAll("selected");

            if (mapButton.getText().equals(selectedMapName)) {
                mapButton.getStyleClass().add("selected");
            }
        }
    }


    private void updateThemeButtons() {
        String currentTheme = GameControllerTheme1.getCurrentTheme();

        // Reset tous les styles
        theme1Button.getStyleClass().removeAll("selected");
        theme2Button.getStyleClass().removeAll("selected");

        // Appliquer le style sélectionné
        if (currentTheme.equals("theme1")) {
            theme1Button.getStyleClass().add("selected");
        } else if (currentTheme.equals("theme2")) {
            theme2Button.getStyleClass().add("selected");
        }
    }

    @FXML
    private void handleTheme1Button() {
        selectTheme("theme1", "Thème Classique");
    }

    @FXML
    private void handleTheme2Button() {
        selectTheme("theme2", "Thème 2");
    }

    @FXML
    private void handleTheme3Button() {
        selectTheme("theme3", "Thème 3");
    }

    @FXML
    private void handleThemeApplyButton() {
        Alert alert = createStyledAlert("Configuration appliquée",
                "Paramètres sauvegardés",
                "✅ Configuration appliquée !\n\n" +
                        "Thème : " + GameControllerTheme1.getCurrentTheme().toUpperCase() + "\n" +
                        "Map : " + selectedMapName + "\n\n" +
                        "Changements effectifs à la prochaine partie.");
        alert.showAndWait();

        hideThemeView();
    }

    @FXML
    private void handleThemeCloseButton() {
        hideThemeView();
    }
    // NOUVELLE MÉTHODE : Sélectionner un thème
    private void selectTheme(String themeId, String themeName) {
        String oldTheme = GameControllerTheme1.getCurrentTheme();

        if (!themeId.equals(oldTheme)) {
            GameControllerTheme1.setCurrentTheme(themeId);
            updateThemeButtons();
        }
    }

    // Méthodes statiques pour l'accès externe
    public static String getSelectedMapName() {
        return selectedMapName;
    }

    public static void setSelectedMapName(String mapName) {
        selectedMapName = mapName;
    }


    private void handleMapEditor() {

        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            double originalWidth = stage.getWidth();
            double originalHeight = stage.getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mapeditor.fxml"));
            Parent editorRoot = loader.load();

            MapEditorController editorController = loader.getController();

            editorController.setOriginalDimensions(originalWidth, originalHeight);

            Scene editorScene = new Scene(editorRoot, 1000, 700);


            editorScene.getStylesheets().add(getClass().getResource("/css/mapeditor.css").toExternalForm());

            stage.setScene(editorScene);
            stage.setTitle("Super Bomberman - Éditeur de Cartes");
            stage.setWidth(1000);
            stage.setHeight(700);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
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

    // Sound effect methods
    private void playNavigationSound() {
        URL navigationSound = getClass().getResource("/Sound/navigation.mp3");
        if (navigationSound != null) {
            playSound("navigation.mp3");
        } else {
            playSound("select.mp3");
        }
    }

    private void playSelectionSound() {
        URL selectionSound = getClass().getResource("/Sound/select.mp3");
        if (selectionSound != null) {
            playSound("select.mp3");
        } else {
            playSound("navigation.mp3");
        }
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
        // Ne pas arrêter la musique ici pour qu'elle continue dans le jeu
    }

    // Getters for testing or external access
    public int getSelectedIndex() {
        return selectedIndex;
    }

    public int getMenuOptionsCount() {
        return menuOptions.size();
    }
}
