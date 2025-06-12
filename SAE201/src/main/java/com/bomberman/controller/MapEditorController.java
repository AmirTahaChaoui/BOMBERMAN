package com.bomberman.controller;

import com.bomberman.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.bomberman.model.CustomMap;

public class MapEditorController implements Initializable {

    private MapManager mapManager;

    @FXML private StackPane root;
    @FXML private GridPane mapGrid;

    // Informations
    @FXML private Label mapSizeLabel;
    @FXML private TextField mapNameField;

    // Constantes
    private static final int CELL_SIZE = 30;
    private static final int MAP_WIDTH = 15;
    private static final int MAP_HEIGHT = 13;

    private double originalWidth = 800;  // Valeurs par défaut
    private double originalHeight = 600;

    // État de l'éditeur
    private int[][] mapData;
    private Rectangle[][] mapCells;

    // Images
    private Image wallImage;
    private Image floorImage;
    private Image blockImage;

    // Gestionnaires
    private UserManager userManager;

    /**
     * Initialise le contrôleur : charge les images du thème, prépare la grille de map,
     * initialise les données et configure les raccourcis clavier.
     *
     * @param location  emplacement du fichier FXML (non utilisé)
     * @param resources ressources pour la localisation (non utilisé)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userManager = UserManager.getInstance();
        mapManager = MapManager.getInstance();

        loadImages();
        initializeMapData();
        createMapGrid();
        setupDefaults(); // ✅ RENOMMÉ : setupTools() -> setupDefaults()
        setupKeyboardControls();
        updateInfoPanel();
    }

    /**
     * Charge les images de textures (mur, sol, bloc destructible) selon le thème en cours.
     */
    private void loadImages() {
        String currentTheme = GameController.getCurrentTheme();
        String themePath = "/images/" + currentTheme + "/";

        wallImage = new Image(getClass().getResource(themePath + "wall.png").toExternalForm());
        floorImage = new Image(getClass().getResource(themePath + "floor.png").toExternalForm());
        blockImage = new Image(getClass().getResource(themePath + "block.png").toExternalForm());
    }

    /**
     * Définit les dimensions d'origine du menu principal pour les restaurer lors du retour.
     *
     * @param width  largeur d'origine
     * @param height hauteur d'origine
     */
    public void setOriginalDimensions(double width, double height) {
        this.originalWidth = width;
        this.originalHeight = height;
    }

    /**
     * Initialise les données de la map avec une grille vide entourée de murs indestructibles.
     */
    private void initializeMapData() {
        mapData = new int[MAP_HEIGHT][MAP_WIDTH];
        mapCells = new Rectangle[MAP_HEIGHT][MAP_WIDTH];

        // Créer une map vide par défaut avec bordures
        for (int row = 0; row < MAP_HEIGHT; row++) {
            for (int col = 0; col < MAP_WIDTH; col++) {
                if (row == 0 || row == MAP_HEIGHT - 1 || col == 0 || col == MAP_WIDTH - 1) {
                    mapData[row][col] = 1; // Bordures = murs
                } else {
                    mapData[row][col] = 0; // Intérieur = vide
                }
            }
        }
    }

    /**
     * Génère la grille graphique de l'éditeur à partir des données de la map,
     * et attache les événements de clic et survol aux cellules.
     */
    private void createMapGrid() {
        mapGrid.getChildren().clear();

        for (int row = 0; row < MAP_HEIGHT; row++) {
            for (int col = 0; col < MAP_WIDTH; col++) {
                Rectangle cell = createCell(row, col);
                mapCells[row][col] = cell;
                mapGrid.add(cell, col, row);

                // Ajouter les gestionnaires d'événements
                final int finalRow = row;
                final int finalCol = col;

                cell.setOnMouseClicked(e -> handleCellClick(finalRow, finalCol, e));
                cell.setOnMouseEntered(e -> {
                    if (e.isPrimaryButtonDown()) {
                        handleCellClick(finalRow, finalCol, e);
                    }
                });
            }
        }
    }

    /**
     * Crée un rectangle représentant une cellule de la grille à la position spécifiée.
     *
     * @param row ligne de la cellule
     * @param col colonne de la cellule
     * @return le rectangle représentant la cellule
     */
    private Rectangle createCell(int row, int col) {
        Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
        updateCellAppearance(cell, mapData[row][col]);

        // Style de base
        cell.setStroke(Color.GRAY);
        cell.setStrokeWidth(0.5);

        return cell;
    }

    /**
     * Met à jour l’apparence graphique d’une cellule en fonction de son type (vide, mur, bloc).
     *
     * @param cell     cellule à modifier
     * @param cellType type de la cellule (0 = vide, 1 = mur, 2 = bloc destructible)
     */
    private void updateCellAppearance(Rectangle cell, int cellType) {
        switch (cellType) {
            case 0: // Vide
                if (floorImage != null) {
                    cell.setFill(new ImagePattern(floorImage));
                } else {
                    cell.setFill(Color.LIGHTGRAY);
                }
                break;
            case 1: // Mur indestructible
                if (wallImage != null) {
                    cell.setFill(new ImagePattern(wallImage));
                } else {
                    cell.setFill(Color.DARKBLUE);
                }
                break;
            case 2: // Mur destructible
                if (blockImage != null) {
                    cell.setFill(new ImagePattern(blockImage));
                } else {
                    cell.setFill(Color.BROWN);
                }
                break;
            default:
                cell.setFill(Color.PINK); // Erreur
        }
    }

    /**
     * Configure les valeurs par défaut de l’éditeur (nom de map, utilisateur, etc.).
     */
    private void setupDefaults() {
        // Nom de map par défaut
        if (userManager.isLoggedIn()) {
            User user = userManager.getCurrentUser();
            mapNameField.setText("Map_" + user.getUsername());
        } else {
            mapNameField.setText("Map_Custom");
        }
    }

    /**
     * Configure les raccourcis clavier pour sauvegarder, charger ou quitter l’éditeur.
     */
    private void setupKeyboardControls() {
        root.setOnKeyPressed(this::handleKeyPress);
        root.setFocusTraversable(true);
        root.requestFocus();
    }

    /**
     * Gère les raccourcis clavier pressés dans l’éditeur (Ctrl+S, Ctrl+L, Échap).
     *
     * @param event événement clavier capturé
     */
    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();

        switch (code) {
            case S:
                if (event.isControlDown()) {
                    saveMap();
                }
                break;
            case L:
                if (event.isControlDown()) {
                    loadMap();
                }
                break;
            case ESCAPE:
                backToMenu();
                break;
        }
        event.consume();
    }

    /**
     * Gère le clic sur une cellule de la grille : bascule entre vide et mur indestructible.
     *
     * @param row   ligne de la cellule
     * @param col   colonne de la cellule
     * @param event événement de souris déclenché
     */
    private void handleCellClick(int row, int col, MouseEvent event) {
        // Ne pas modifier les bordures
        if (row == 0 || row == MAP_HEIGHT - 1 || col == 0 || col == MAP_WIDTH - 1) {
            return;
        }

        // Protéger les zones de spawn
        if (isSpawnZone(row, col)) {
            return;
        }

        // ✅ LOGIQUE SIMPLE : Basculer entre vide (0) et mur indestructible (1)
        if (event.isPrimaryButtonDown() || event.getEventType() == MouseEvent.MOUSE_CLICKED) {
            if (mapData[row][col] == 1) {
                // Si c'est un mur indestructible, le supprimer
                mapData[row][col] = 0;
            } else {
                // Sinon, placer un mur indestructible
                mapData[row][col] = 1;
            }

            updateCellAppearance(mapCells[row][col], mapData[row][col]);
        }
    }

    /**
     * Met à jour l'affichage des informations de la map (dimensions, etc.).
     */
    private void updateInfoPanel() {
        mapSizeLabel.setText("Taille: " + MAP_WIDTH + "x" + MAP_HEIGHT);
    }

    /**
     * Sauvegarde la map actuelle après validation du nom, génération des murs destructibles
     * et vérification de la validité. Affiche des alertes selon le résultat.
     */
    @FXML
    private void saveMap() {
        String mapName = mapNameField.getText().trim();

        if (mapName.isEmpty()) {
            Alert alert = createStyledAlert(Alert.AlertType.WARNING, "Erreur", "Nom de map requis",
                    "Veuillez saisir un nom pour votre map.");
            alert.showAndWait();
            return;
        }

        List<String> existingMaps = mapManager.getMapsList();
        boolean mapExists = existingMaps.contains(mapName);

        if (mapExists) {
            Alert confirm = createStyledAlert(Alert.AlertType.CONFIRMATION, "Map existante",
                    "Une map avec ce nom existe déjà", "Voulez-vous la remplacer ?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        generateDestructibleWalls();
        int[][] matrix = createMatrixFromEditor();

        String author = userManager.isLoggedIn() ?
                userManager.getCurrentUser().getUsername() : "Anonyme";

        CustomMap newMap = new CustomMap(mapName, "Map créée avec l'éditeur",
                MAP_WIDTH, MAP_HEIGHT, matrix, author);

        if (!newMap.isValid()) {
            Alert alert = createStyledAlert(Alert.AlertType.ERROR, "Erreur", "Map invalide",
                    "La map doit contenir au moins 2 zones de spawn pour les joueurs");
            alert.showAndWait();
            return;
        }

        boolean success = mapManager.saveMap(newMap);

        if (success) {
            Alert alert = createStyledAlert(Alert.AlertType.INFORMATION, "Succès", "Map sauvegardée",
                    "Map '" + mapName + "' sauvegardée avec succès !\n" +
                            "Total maps : " + mapManager.getMapCount());
            alert.showAndWait();
        } else {
            Alert alert = createStyledAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de sauvegarde",
                    "Impossible de sauvegarder la map");
            alert.showAndWait();
        }
    }

    /**
     * Remplit toutes les cellules non protégées avec des murs destructibles.
     * Ignore les bordures et les zones de spawn.
     */
    private void generateDestructibleWalls() {
        for (int row = 0; row < MAP_HEIGHT; row++) {
            for (int col = 0; col < MAP_WIDTH; col++) {
                // Ne pas toucher aux bordures (murs indestructibles)
                if (row == 0 || row == MAP_HEIGHT - 1 || col == 0 || col == MAP_WIDTH - 1) {
                    continue;
                }

                // Ne pas toucher aux zones de spawn
                if (isSpawnZone(row, col)) {
                    mapData[row][col] = 0; // Garder vide
                    continue;
                }

                // Si ce n'est pas un mur indestructible, placer un mur destructible
                if (mapData[row][col] != 1) {
                    mapData[row][col] = 2; // Mur destructible
                }
            }
        }
        // Mettre à jour l'affichage
        createMapGrid();
    }

    /**
     * Construit une matrice 2D représentant la map actuelle à partir de l'état de l'éditeur.
     *
     * @return une matrice d'entiers correspondant aux types de chaque cellule
     */
    private int[][] createMatrixFromEditor() {
        // Utiliser les constantes de l'éditeur pour garantir la bonne taille
        int[][] matrix = new int[MAP_HEIGHT][MAP_WIDTH];

        for (int row = 0; row < MAP_HEIGHT; row++) {
            for (int col = 0; col < MAP_WIDTH; col++) {
                // Vérifier que nous sommes dans les limites des données de l'éditeur
                if (row < mapData.length && col < mapData[row].length) {
                    matrix[row][col] = mapData[row][col];
                } else {
                    // Remplir avec des murs si hors limites (bordures)
                    matrix[row][col] = 1;
                }
            }
        }

        return matrix;
    }

    /**
     * Vérifie si une cellule appartient à une zone de spawn (haut-gauche ou bas-droite).
     *
     * @param row ligne
     * @param col colonne
     * @return {@code true} si la cellule est une zone de spawn, sinon {@code false}
     */
    private boolean isSpawnZone(int row, int col) {
        // Zone spawn joueur 1 (coin haut-gauche)
        if ((row == 1 && col == 1) || (row == 1 && col == 2) || (row == 2 && col == 1)) {
            return true;
        }

        // Zone spawn joueur 2 (coin bas-droite)
        if ((row == MAP_HEIGHT-2 && col == MAP_WIDTH-2) ||
                (row == MAP_HEIGHT-2 && col == MAP_WIDTH-3) ||
                (row == MAP_HEIGHT-3 && col == MAP_WIDTH-2)) {
            return true;
        }

        return false;
    }

    /**
     * Ouvre une boîte de dialogue pour sélectionner une map enregistrée et la charger
     * dans l’éditeur si elle est valide.
     */
    @FXML
    private void loadMap() {
        List<String> mapNames = mapManager.getMapsList();

        if (mapNames.isEmpty()) {
            Alert alert = createStyledAlert(Alert.AlertType.INFORMATION, "Info", "Aucune map",
                    "Aucune map sauvegardée trouvée.");
            alert.showAndWait();
            return;
        }

        // Dialog de sélection stylé
        ChoiceDialog<String> dialog = createStyledChoiceDialog(mapNames.get(0), mapNames,
                "Charger une map", "Sélectionnez une map à charger", "Maps disponibles :");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            loadMapData(result.get());
        }
    }

    /**
     * Charge les données d’une map existante par son nom et les affiche dans l’éditeur.
     *
     * @param mapName nom de la map à charger
     */
    private void loadMapData(String mapName) {
        try {
            CustomMap map = mapManager.getMapByName(mapName);
            if (map == null) {
                Alert alert = createStyledAlert(Alert.AlertType.ERROR, "Erreur", "Map introuvable",
                        "Impossible de charger la map '" + mapName + "'");
                alert.showAndWait();
                return;
            }

            // Charger les données de la map
            mapData = map.getMatrixCopy();
            mapNameField.setText(map.getName());

            // Recréer la grille visuelle
            createMapGrid();

            Alert alert = createStyledAlert(Alert.AlertType.INFORMATION, "Succès", "Map chargée",
                    "Map '" + map.getName() + "' chargée avec succès !\n" +
                            "Auteur : " + map.getAuthor() + "\n" +
                            "Taille : " + map.getWidth() + "x" + map.getHeight() + "\n" +
                            "Créée le : " + map.getCreated().substring(0, 10));
            alert.showAndWait();

            System.out.println("📂 Map chargée: " + map.getName());

        } catch (Exception e) {
            Alert alert = createStyledAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement",
                    "Impossible de charger la map:\n" + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    /**
     * Réinitialise la map en affichant une confirmation à l'utilisateur.
     * Supprime tout contenu en recréant une map vide par défaut.
     */
    @FXML
    private void clearMap() {
        Alert confirm = createStyledAlert(Alert.AlertType.CONFIRMATION, "Effacer la map",
                "Êtes-vous sûr ?", "Cette action effacera toute la map actuelle.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            initializeMapData();
            createMapGrid();
        }
    }

    /**
     * Retourne à la scène du menu principal en restaurant les dimensions originales de la fenêtre.
     */
    @FXML
    private void backToMenu() {
        try {
            // Charger la scène du menu principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent menuRoot = loader.load();

            // Créer la scène du menu
            Scene menuScene = new Scene(menuRoot, originalWidth, originalHeight);
            menuScene.getStylesheets().add(getClass().getResource("/css/menu.css").toExternalForm());

            // Retourner à la fenêtre du menu avec les bonnes dimensions
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(menuScene);
            stage.setTitle("Super Bomberman - Menu");

            // REMETTRE les dimensions originales
            stage.setWidth(originalWidth);
            stage.setHeight(originalHeight);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Crée une alerte personnalisée avec un style CSS adapté à l’éditeur.
     *
     * @param type    Type de l'alerte (INFO, WARNING, etc.)
     * @param title   Titre
     * @param header  En-tête
     * @param content Corps du message
     * @return l'objet {@link Alert} configuré
     */
    private Alert createStyledAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // ✅ AJOUTER LE CSS
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/mapeditor.css").toExternalForm()
        );

        return alert;
    }

    /**
     * Crée une boîte de dialogue de choix personnalisée avec les styles CSS de l'éditeur.
     *
     * @param defaultChoice choix sélectionné par défaut
     * @param choices       liste des choix disponibles
     * @param title         titre de la boîte
     * @param header        en-tête
     * @param content       description du contenu
     * @return la boîte de dialogue {@link ChoiceDialog}
     */
    private ChoiceDialog<String> createStyledChoiceDialog(String defaultChoice, List<String> choices,
                                                          String title, String header, String content) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(defaultChoice, choices);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);

        // ✅ AJOUTER LE CSS
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/mapeditor.css").toExternalForm()
        );

        return dialog;
    }

}