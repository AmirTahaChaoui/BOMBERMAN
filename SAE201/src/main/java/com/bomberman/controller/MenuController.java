package com.bomberman.controller;

import com.bomberman.controller.MusicManager;
import com.bomberman.controller.UserManager;
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MenuController implements Initializable {

    // FXML Elements existants
    @FXML private StackPane root;
    @FXML private VBox menuContainer;
    @FXML private VBox menuButtons;
    @FXML private ImageView titleImage;
    @FXML private Label gameTitle;
    @FXML private Label gameSubtitle;
    @FXML private Label versionLabel;
    @FXML private Label controlsLabel;

    // Button containers existants
    @FXML private HBox playButtonContainer;
    @FXML private HBox settingsButtonContainer;
    @FXML private HBox rankingButtonContainer; // NOUVEAU
    @FXML private HBox loginButtonContainer;
    @FXML private HBox exitButtonContainer;

    // Buttons existants
    @FXML private Button playButton;
    @FXML private Button settingsButton;
    @FXML private Button rankingButton; // NOUVEAU
    @FXML private Button loginButton;
    @FXML private Button exitButton;

    // Cursors existants
    @FXML private Label playCursor;
    @FXML private Label settingsCursor;
    @FXML private Label rankingCursor; // NOUVEAU
    @FXML private Label loginCursor;
    @FXML private Label exitCursor;

    // NOUVEAUX ÉLÉMENTS - Système de connexion
    @FXML private StackPane loginView;
    @FXML private VBox loginContent;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button connectButton;
    @FXML private Button cancelButton;
    @FXML private Button createAccountLink;
    @FXML private Label loginErrorLabel;

    // NOUVEAUX ÉLÉMENTS - Système d'inscription
    @FXML private StackPane registerView;
    @FXML private VBox registerContent;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField registerUsernameField;
    @FXML private PasswordField registerPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> avatarComboBox;
    @FXML private Button createAccountButton;
    @FXML private Button cancelRegisterButton;
    @FXML private Button backToLoginLink;
    @FXML private Label registerErrorLabel;

    // NOUVEAU ÉLÉMENT - Vue du classement
    @FXML private StackPane rankingView;
    @FXML private VBox rankingContent;
    @FXML private Button closeRankingButton;


    // Navigation state existante
    private int selectedIndex = 0;
    private List<MenuOption> menuOptions;
    private Timeline cursorBlinkAnimation;
    private boolean isAnimationRunning = false;

    // NOUVEAU : Gestion des utilisateurs
    private UserManager userManager;

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

        // NOUVEAU : Configurer le système de connexion
        setupLoginSystem();

        // Set initial selection
        updateSelection();

        // Request focus for keyboard navigation
        Platform.runLater(() -> {
            root.requestFocus();
        });
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

        // Configurer les avatars disponibles
        setupAvatarComboBox();
    }

    // NOUVELLE MÉTHODE : Configurer la liste des avatars
    private void setupAvatarComboBox() {
        if (avatarComboBox != null) {
            avatarComboBox.getItems().addAll(
                    "🧑‍💼 Avatar Business",
                    "👨‍🎮 Avatar Gamer",
                    "👩‍🎨 Avatar Artiste",
                    "🧑‍🚀 Avatar Astronaute",
                    "👨‍🔬 Avatar Scientifique",
                    "👩‍🏫 Avatar Professeur",
                    "🧑‍🍳 Avatar Chef",
                    "👨‍⚕️ Avatar Médecin"
            );
            avatarComboBox.getSelectionModel().selectFirst(); // Sélectionner le premier par défaut
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

        // DEBUG : Afficher les infos de connexion
        System.out.println("=== TENTATIVE DE CONNEXION ===");
        System.out.println("Username saisi : '" + username + "'");
        System.out.println("Password saisi : '" + password + "'");
        System.out.println("Nombre d'utilisateurs : " + userManager.getUserCount());

        // Vider le message d'erreur
        loginErrorLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            showLoginError("Veuillez remplir tous les champs");
            return;
        }

        // Tentative de connexion
        boolean success = userManager.login(username, password);

        System.out.println("Résultat connexion : " + success);

        if (success) {
            System.out.println("✅ Connexion réussie !");
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
        System.out.println("Lien créer un compte cliqué");
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
        String selectedAvatar = avatarComboBox.getSelectionModel().getSelectedItem();

        // DEBUG : Afficher les infos de création
        System.out.println("=== CRÉATION DE COMPTE ===");
        System.out.println("Prénom : '" + firstName + "'");
        System.out.println("Nom : '" + lastName + "'");
        System.out.println("Username : '" + username + "'");
        System.out.println("Password : '" + password + "'");
        System.out.println("Avatar : '" + selectedAvatar + "'");

        // Vider le message d'erreur
        registerErrorLabel.setVisible(false);

        // Validation des champs
        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty() || selectedAvatar == null) {
            showRegisterError("Veuillez remplir tous les champs");
            return;
        }

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

        // Extraire l'ID de l'avatar (premier mot après l'emoji)
        String avatarId = selectedAvatar.split(" ")[1].toLowerCase(); // Ex: "business", "gamer", etc.

        System.out.println("Avatar ID : '" + avatarId + "'");

        // Tentative de création du compte
        boolean success = userManager.createUser(username, password, firstName, lastName, avatarId);

        System.out.println("Résultat création : " + success);
        System.out.println("Nombre d'utilisateurs après création : " + userManager.getUserCount());

        if (success) {
            System.out.println("✅ Compte créé avec succès !");

            // Connexion automatique après création
            boolean loginSuccess = userManager.login(username, password);
            System.out.println("Connexion automatique : " + loginSuccess);

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
        if (avatarComboBox.getItems().size() > 0) {
            avatarComboBox.getSelectionModel().selectFirst();
        }
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
            System.out.println("👋 Déconnexion réussie");
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
                (rankingView != null && rankingView.isVisible());
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
        // Ne pas traiter les touches si une vue de connexion/inscription est visible
        if ((loginView != null && loginView.isVisible()) ||
                (registerView != null && registerView.isVisible()) ||
                (rankingView != null && rankingView.isVisible())) {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (loginView.isVisible()) {
                    handleCancelButton();
                } else if (registerView.isVisible()) {
                    handleCancelRegisterButton();
                } else if (rankingView.isVisible()) {
                    handleCloseRankingButton();
                }
            }
            return;
        }

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
            System.out.println("♪ Musique en pause");
        } else {
            musicManager.resumeBackgroundMusic();
            System.out.println("♪ Musique reprise");
        }
    }

    // Méthode générique pour jouer un son (effets sonores)
    private void playSound(String soundFileName) {
        try {
            URL soundUrl = getClass().getResource("/Sound/" + soundFileName);
            if (soundUrl == null) {
                System.out.println("Fichier son non trouvé: /Sound/" + soundFileName);
                return;
            }

            String musicFile = soundUrl.toExternalForm();
            Media media = new Media(musicFile);
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(0.7); // Volume plus fort pour les effets sonores
            mediaPlayer.play();

        } catch (Exception e) {
            System.out.println("Erreur lors du chargement du son " + soundFileName + ": " + e.getMessage());
        }
    }

    // Méthode publique pour compatibility
    public void playSound() {
        playSound("select.mp3");
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
        System.out.println("Demarrage du jeux ...");

        try {
            // Load the game scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/theme1.fxml"));
            Parent gameRoot = loader.load();

            Scene gameScene = new Scene(gameRoot, 800, 700);
            gameScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) playButton.getScene().getWindow();
            stage.setScene(gameScene);
            stage.setTitle("Super Bomberman - Jeux");

            // Stop menu animations but keep music playing
            shutdown();
            // La musique continue à jouer dans le jeu !

        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Erreur au niveau du jeux", "Impossible de charger le jeux.",
                    "Verifie que le game.fxml existes belle est bien dans le resources/fxml.");
        }
    }

    @FXML
    private void handleSettingsButton() {
        System.out.println("Opening settings...");

        String musicInfo = String.format("Musique: %s (Vol: %.0f%%)\nPiste actuelle: %s",
                musicManager.isPlaying() ? "Activée" : "Désactivée",
                musicManager.getVolume() * 100,
                musicManager.getCurrentTrackName());

        Alert alert = createStyledAlert( "Settings",
                "Parametre du jeux",
                "Resolution: 800x600\n" +
                        musicInfo + "\n" +
                        "Controles: Fleche + Espace\n" +
                        "Difficulté: Normal\n" +
                        "Controles musique:\n" +
                        "M = Pause/Reprise\n" +
                        "N = Piste suivante\n" +
                        "P = Piste précédente\n\n" +
                        "Parametre configuration prochainement!");
        alert.showAndWait();
    }

    // NOUVELLES MÉTHODES : Gestionnaires de connexion
    @FXML
    private void handleLoginButton() {
        System.out.println("Bouton connexion/compte cliqué");

        if (userManager.isLoggedIn()) {
            // Si connecté, afficher le profil/déconnexion
            showUserProfile();
        } else {
            // Si pas connecté, afficher la fenêtre de connexion
            showLoginView();
        }
    }

    @FXML
    private void handleExitButton() {
        System.out.println("Fermeture du jeux...");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Fermeture du jeux");
        alert.setHeaderText("Etes vous sur de vouloir quitter ?");
        alert.setContentText("Tous les progres non sauvegrader seront perdue.");

        // Apply custom styling
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/menu.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add("alert");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            shutdown();
            musicManager.shutdown(); // Arrêter la musique à la fermeture
            Platform.exit();
        }
    }

    // NOUVELLE MÉTHODE : Afficher le classement
    @FXML
    private void handleRankingButton() {
        System.out.println("Affichage du classement...");
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

            // Ajouter des médailles pour le top 3

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
        System.out.println("♪ Son de navigation menu");
    }

    private void playSelectionSound() {
        URL selectionSound = getClass().getResource("/Sound/select.mp3");
        if (selectionSound != null) {
            playSound("select.mp3");
        } else {
            playSound("navigation.mp3");
        }
        System.out.println("♪ Son de selection menu");
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