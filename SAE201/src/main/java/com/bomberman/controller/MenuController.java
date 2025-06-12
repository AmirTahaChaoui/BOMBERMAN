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

    /**
     * Initialise le contrôleur du menu principal.
     *
     * @param location  L'emplacement utilisé pour résoudre les chemins relatifs pour l'objet racine.
     * @param resources Les ressources utilisées pour localiser les éléments du fichier FXML.
     */
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

    /**
     * Initialise le système de connexion.
     * Met à jour l'affichage du bouton de connexion et masque les vues de connexion, d'inscription, de thème et de mode de jeu.
     */
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

    /**
     * Met à jour le texte du bouton de connexion selon l'état de connexion de l'utilisateur.
     */
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

    /**
     * Gère l'action du bouton de connexion. Vérifie les identifiants et tente une connexion.
     */
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

    /**
     * Gère l'action du bouton d'annulation dans la vue de connexion.
     * Ferme la vue de connexion et réinitialise les champs.
     */
    @FXML
    private void handleCancelButton() {
        hideLoginView();
        clearLoginFields();
        loginErrorLabel.setVisible(false);
    }

    /**
     * Affiche la vue d'inscription lorsqu'on clique sur le lien "Créer un compte".
     */
    @FXML
    private void handleCreateAccountLink() {
        showRegisterView();
    }

    /**
     * Gère l'action du bouton de création de compte. Vérifie les champs et tente de créer un utilisateur.
     */
    @FXML
    private void handleCreateAccountButton() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String username = registerUsernameField.getText().trim();
        String password = registerPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

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

    /**
     * Gère l'action du bouton d'annulation dans la vue d'inscription.
     * Ferme la vue et réinitialise les champs.
     */
    @FXML
    private void handleCancelRegisterButton() {
        hideRegisterView();
        clearRegisterFields();
        registerErrorLabel.setVisible(false);
    }

    /**
     * Revient à la vue de connexion depuis la vue d'inscription.
     */
    @FXML
    private void handleBackToLoginLink() {
        hideRegisterView();
        showLoginView();
        clearRegisterFields();
    }

    /**
     * Affiche la vue de connexion et désactive la navigation clavier dans le menu principal.
     */
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

    /**
     * Masque la vue de connexion et réactive la navigation clavier du menu principal.
     */
    private void hideLoginView() {
        loginView.setVisible(false);
        // Réactiver la navigation clavier du menu
        root.setFocusTraversable(true);
        Platform.runLater(() -> {
            root.requestFocus();
        });
    }

    /**
     * Affiche la vue d'inscription et désactive la navigation clavier dans le menu principal.
     */
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

    /**
     * Masque la vue d'inscription et réactive la navigation clavier du menu principal.
     */
    private void hideRegisterView() {
        registerView.setVisible(false);
        // Réactiver la navigation clavier du menu
        root.setFocusTraversable(true);
        Platform.runLater(() -> {
            root.requestFocus();
        });
    }

    /**
     * Réinitialise les champs du formulaire de connexion.
     */
    private void clearLoginFields() {
        usernameField.clear();
        passwordField.clear();
    }

    /**
     * Réinitialise les champs du formulaire d'inscription.
     */
    private void clearRegisterFields() {
        firstNameField.clear();
        lastNameField.clear();
        registerUsernameField.clear();
        registerPasswordField.clear();
        confirmPasswordField.clear();
    }

    /**
     * Affiche un message d'erreur dans la vue de connexion.
     *
     * @param message Message à afficher.
     */
    private void showLoginError(String message) {
        loginErrorLabel.setText(message);
        loginErrorLabel.setVisible(true);
    }

    /**
     * Affiche un message d'erreur dans la vue d'inscription.
     *
     * @param message Message à afficher.
     */
    private void showRegisterError(String message) {
        registerErrorLabel.setText(message);
        registerErrorLabel.setVisible(true);
    }

    /**
     * Affiche une boîte de dialogue contenant les informations de l'utilisateur connecté.
     * Propose également une option de déconnexion.
     */
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

    /**
     * Initialise la musique de fond avec un volume prédéfini.
     */
    private void setupMusic() {
        musicManager = MusicManager.getInstance();
        musicManager.setVolume(0.3);
        musicManager.startBackgroundMusic();
    }

    /**
     * Initialise les différentes options du menu principal avec leurs boutons, curseurs et conteneurs.
     */
    private void setupMenuOptions() {
        menuOptions = new ArrayList<>();
        menuOptions.add(new MenuOption(playButton, playCursor, playButtonContainer));
        menuOptions.add(new MenuOption(settingsButton, settingsCursor, settingsButtonContainer));
        menuOptions.add(new MenuOption(loginButton, loginCursor, loginButtonContainer));
        menuOptions.add(new MenuOption(exitButton, exitCursor, exitButtonContainer));
        menuOptions.add(new MenuOption(rankingButton, rankingCursor, rankingButtonContainer)); // NOUVEAU

    }

    /**
     * Met à jour les intitulés des boutons en fonction de l'état actuel (menu principal ou sous-menu).
     */
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

    /**
     * Configure les événements clavier pour naviguer dans le menu.
     */
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

    /**
     * Vérifie si une vue secondaire (connexion, inscription, thème, mode de jeu) est actuellement visible.
     *
     * @return {@code true} si une vue est visible, sinon {@code false}.
     */
    private boolean isAnyViewVisible() {
        return (loginView != null && loginView.isVisible()) ||
                (registerView != null && registerView.isVisible()) ||
                (themeView != null && themeView.isVisible()) ||
                (gameModeView != null && gameModeView.isVisible());
    }

    /**
     * Configure et démarre l'animation de clignotement du curseur de sélection.
     */
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

    /**
     * Affiche le titre texte si l'image du titre échoue à se charger.
     */
    private void setupImageFallback() {
        // Show text title if image fails to load
        titleImage.imageProperty().addListener((obs, oldImage, newImage) -> {
            if (newImage == null || newImage.isError()) {
                titleImage.setVisible(false);
                gameTitle.setVisible(true);
            }
        });
    }

    /**
     * Gère les événements clavier pour naviguer dans le menu ou interagir avec les vues secondaires.
     *
     * @param event L'événement clavier déclenché.
     */
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

    /**
     * Met en pause ou reprend la musique de fond selon l'état actuel.
     */

    private void toggleMusic() {
        if (musicManager.isPlaying()) {
            musicManager.pauseBackgroundMusic();
        } else {
            musicManager.resumeBackgroundMusic();
        }
    }

    /**
     * Joue un fichier audio depuis les ressources du projet.
     *
     * @param soundFileName Nom du fichier son à jouer (avec extension).
     */
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

    /**
     * Sélectionne l'option précédente dans le menu, si possible.
     */

    private void navigateUp() {
        if (selectedIndex > 0) {
            selectedIndex--;
            updateSelection();
            playNavigationSound();
        }
    }

    /**
     * Sélectionne l'option suivante dans le menu, si possible.
     */
    private void navigateDown() {
        if (selectedIndex < menuOptions.size() - 1) {
            selectedIndex++;
            updateSelection();
            playNavigationSound();
        }
    }

    /**
     * Met à jour l'affichage visuel de l'option actuellement sélectionnée dans le menu.
     */
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

    /**
     * Exécute l'action correspondant au bouton actuellement sélectionné.
     */
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

    /**
     * Gère l'action du bouton "Jouer". Lance la partie ou affiche le sous-menu.
     */
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

    /**
     * Lance le processus de démarrage de la partie en affichant la vue de sélection du mode de jeu.
     */
    private void startGame() {
        // NOUVEAU : Ouvrir la vue de sélection de mode au lieu de lancer directement
        showGameModeView();
    }

    /**
     * Affiche la vue de sélection du mode de jeu et désactive la navigation clavier dans le menu principal.
     */
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

    /**
     * Masque la vue de sélection du mode de jeu et réactive la navigation clavier dans le menu principal.
     */
    private void hideGameModeView() {
        gameModeView.setVisible(false);

        // Réactiver la navigation clavier du menu
        root.setFocusTraversable(true);

        Platform.runLater(() -> {
            root.requestFocus();
        });
    }

    /**
     * Gère le lancement du mode normal de jeu.
     */
    @FXML
    private void handleNormalModeButton() {
        hideGameModeView();
        launchNormalMode();
    }

    /**
     * Gère le lancement du mode Capture the Flag.
     */
    @FXML
    private void handleCaptureFlagModeButton() {
        hideGameModeView();
        launchCaptureTheFlagMode();
    }

    /**
     * Gère le retour depuis la sélection du mode de jeu vers le menu.
     */
    @FXML
    private void handleGameModeBackButton() {
        hideGameModeView();
    }

    /**
     * Lance le mode de jeu normal en chargeant la scène correspondante.
     * Transmet les dimensions du menu et la carte sélectionnée au contrôleur du jeu.
     * Affiche une erreur si le fichier FXML est introuvable.
     */
    private void launchNormalMode() {
        try {
            // Passer la map sélectionnée au GameController
            GameController.setSelectedMap(selectedMapName);

            // Sauvegarder les dimensions actuelles du menu
            Stage stage = (Stage) playButton.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();

            // Passer les dimensions au GameController
            GameController.setOriginalMenuDimensions(currentWidth, currentHeight);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
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
                    "Vérifiez que le fichier game.fxml existe dans resources/fxml/");
        }
    }

    /**
     * Lance le mode "Capture du Drapeau" en chargeant la scène correspondante.
     * Transmet la carte et le thème sélectionnés au contrôleur, ainsi que les dimensions du menu.
     * Affiche une erreur dans la console en cas d'échec de chargement.
     */
    private void launchCaptureTheFlagMode() {
        try {
            // Passer les données nécessaires au CaptureTheFlagController
            CaptureTheFlagController.setSelectedMap(selectedMapName);
            CaptureTheFlagController.setCurrentTheme(GameController.getCurrentTheme());

            // Sauvegarder les dimensions actuelles du menu
            Stage stage = (Stage) playButton.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();

            CaptureTheFlagController.setOriginalMenuDimensions(currentWidth, currentHeight);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/game.fxml"));
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


    /**
     * Gère l'action du bouton "Paramètres". Affiche les infos de configuration ou lance la sélection de thème.
     */
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

    /**
     * Gère l'action du bouton "Se connecter". Affiche la vue de connexion ou le profil si déjà connecté.
     */
    @FXML
    private void handleLoginButton() {
        if (isInSubMenu) {
            handleMapEditor();
        } else {
            if (userManager.isLoggedIn()) {
                showUserProfile();
            } else {
                showLoginView();
            }
        }
    }

    /**
     * Gère l'action du bouton "Quitter" ou "Retour" selon le menu affiché.
     */
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

    /**
     * Gère l'affichage du classement des joueurs.
     */
    @FXML
    private void handleRankingButton() {
        showRankingView();
    }

    /**
     * Affiche une alerte contenant le classement des joueurs en fonction de leurs victoires et de leur ratio.
     * Trie les joueurs et génère dynamiquement le contenu de l'alerte.
     */
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

    /**
     * Masque la vue du classement et réactive la navigation clavier.
     */
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



    /**
     * Gère l'action de sélection de thème en affichant la vue des thèmes.
     */
    private void handleThemeSelection() {
        showThemeView();
    }

    /**
     * Affiche la vue des thèmes et charge dynamiquement la liste des cartes disponibles.
     * Désactive la navigation clavier du menu principal.
     */
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

    /**
     * Masque la vue des thèmes et réactive la navigation clavier.
     */
    private void hideThemeView() {
        themeView.setVisible(false);

        // IMPORTANT : Réactiver la navigation clavier du menu
        root.setFocusTraversable(true);

        Platform.runLater(() -> {
            root.requestFocus();
        });
    }

    /**
     * Charge dynamiquement les cartes disponibles à partir du gestionnaire de cartes
     * et crée un bouton pour chacune dans l'interface.
     */
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

    /**
     * Sélectionne la carte spécifiée et met à jour l'état visuel des boutons de carte.
     *
     * @param mapName Nom de la carte sélectionnée.
     * @param clickedButton Le bouton qui a déclenché la sélection.
     */
    private void selectMap(String mapName, Button clickedButton) {
        // Changer la sélection
        selectedMapName = mapName;

        // Mettre à jour l'apparence des boutons
        updateMapButtonsSelection();
    }

    /**
     * Met à jour l'apparence des boutons de carte pour refléter la carte actuellement sélectionnée.
     */
    private void updateMapButtonsSelection() {
        for (Button mapButton : mapButtons) {
            mapButton.getStyleClass().removeAll("selected");

            if (mapButton.getText().equals(selectedMapName)) {
                mapButton.getStyleClass().add("selected");
            }
        }
    }

    /**
     * Met à jour l'apparence des boutons de thème en fonction du thème actuellement sélectionné.
     */
    private void updateThemeButtons() {
        String currentTheme = GameController.getCurrentTheme();

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

    /**
     * Sélectionne le thème correspondant pour le jeu.
     */
    @FXML
    private void handleTheme1Button() {
        selectTheme("theme1", "Thème Classique");
    }

    /**
     * Sélectionne le thème correspondant pour le jeu.
     */
    @FXML
    private void handleTheme2Button() {
        selectTheme("theme2", "Thème 2");
    }

    /**
     * Sélectionne le thème correspondant pour le jeu.
     */
    @FXML
    private void handleTheme3Button() {
        selectTheme("theme3", "Thème 3");
    }

    /**
     * Applique le thème et la carte sélectionnés, puis ferme la vue des thèmes.
     */
    @FXML
    private void handleThemeApplyButton() {
        Alert alert = createStyledAlert("Configuration appliquée",
                "Paramètres sauvegardés",
                "✅ Configuration appliquée !\n\n" +
                        "Thème : " + GameController.getCurrentTheme().toUpperCase() + "\n" +
                        "Map : " + selectedMapName + "\n\n" +
                        "Changements effectifs à la prochaine partie.");
        alert.showAndWait();

        hideThemeView();
    }

    /**
     * Ferme la vue des thèmes sans appliquer les modifications.
     */
    @FXML
    private void handleThemeCloseButton() {
        hideThemeView();
    }
    // NOUVELLE MÉTHODE : Sélectionner un thème
    private void selectTheme(String themeId, String themeName) {
        String oldTheme = GameController.getCurrentTheme();

        if (!themeId.equals(oldTheme)) {
            GameController.setCurrentTheme(themeId);
            updateThemeButtons();
        }
    }

    /**
     * Retourne le nom de la carte actuellement sélectionnée.
     *
     * @return Nom de la carte sélectionnée.
     */
    public static String getSelectedMapName() {
        return selectedMapName;
    }

    /**
     * Définit le nom de la carte sélectionnée.
     *
     * @param mapName Nom de la carte à définir comme sélectionnée.
     */
    public static void setSelectedMapName(String mapName) {
        selectedMapName = mapName;
    }

    /**
     * Ouvre l'éditeur de carte dans une nouvelle scène.
     * Transmet les dimensions actuelles de la fenêtre au contrôleur de l'éditeur.
     */
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

    /**
     * Crée une alerte personnalisée avec un style CSS spécifique.
     *
     * @param title   Le titre de la boîte de dialogue.
     * @param header  L'en-tête de l'alerte.
     * @param content Le contenu principal du message.
     * @return L'alerte stylisée prête à être affichée.
     */
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

    /**
     * Affiche une boîte de dialogue d'erreur avec un style personnalisé.
     *
     * @param title   Le titre de la boîte de dialogue.
     * @param header  L'en-tête du message d'erreur.
     * @param content Le contenu du message d'erreur.
     */
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

    /**
     * Joue le son associé à la navigation dans le menu.
     * Utilise un son par défaut si le fichier spécifique est introuvable.
     */
    private void playNavigationSound() {
        URL navigationSound = getClass().getResource("/Sound/navigation.mp3");
        if (navigationSound != null) {
            playSound("navigation.mp3");
        } else {
            playSound("select.mp3");
        }
    }

    /**
     * Joue le son associé à la sélection d'une option du menu.
     * Utilise un son alternatif si le fichier est manquant.
     */
    private void playSelectionSound() {
        URL selectionSound = getClass().getResource("/Sound/select.mp3");
        if (selectionSound != null) {
            playSound("select.mp3");
        } else {
            playSound("navigation.mp3");
        }
    }

    /**
     * Arrête les animations du menu. N’arrête pas la musique.
     */
    public void shutdown() {
        if (cursorBlinkAnimation != null && isAnimationRunning) {
            cursorBlinkAnimation.stop();
            isAnimationRunning = false;
        }
    }

    // Getters

    /**
     * Retourne l’index actuellement sélectionné dans le menu.
     *
     * @return Index de l’option sélectionnée.
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Retourne le nombre total d’options dans le menu.
     *
     * @return Nombre d’options du menu.
     */
    public int getMenuOptionsCount() {
        return menuOptions.size();
    }
}
