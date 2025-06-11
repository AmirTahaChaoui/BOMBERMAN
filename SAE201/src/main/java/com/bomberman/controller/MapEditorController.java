package com.bomberman.controller;

import com.bomberman.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.bomberman.controller.MapManager;
import com.bomberman.model.CustomMap;

public class MapEditorController implements Initializable {

    private MapManager mapManager;

    @FXML private StackPane root;
    @FXML private GridPane mapGrid;
    @FXML private VBox toolPanel;
    @FXML private VBox infoPanel;

    // ✅ SUPPRIMÉ : Outils de sélection
    // Les RadioButton et ToggleGroup ont été retirés

    // Informations
    @FXML private Label mapSizeLabel;
    @FXML private TextField mapNameField;

    // Boutons d'action
    @FXML private Button saveButton;
    @FXML private Button loadButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;
    @FXML private Button testButton;

    // Constantes
    private static final int CELL_SIZE = 30;
    private static final int MAP_WIDTH = 15;
    private static final int MAP_HEIGHT = 13;

    private double originalWidth = 800;  // Valeurs par défaut
    private double originalHeight = 600;

    // État de l'éditeur
    private int[][] mapData;
    private Rectangle[][] mapCells;
    // ✅ SUPPRIMÉ : currentTool (plus besoin de sélection d'outil)

    // Images
    private Image wallImage;
    private Image floorImage;
    private Image blockImage;

    // Gestionnaires
    private UserManager userManager;

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

        System.out.println("🗺️ Map Editor simplifié initialisé");
    }

    private void loadImages() {
        try {
            // Charger les images du thème actuel
            String currentTheme = GameControllerTheme1.getCurrentTheme();
            String themePath = "/images/" + currentTheme + "/";

            wallImage = new Image(getClass().getResource(themePath + "wall.png").toExternalForm());
            floorImage = new Image(getClass().getResource(themePath + "floor.png").toExternalForm());
            blockImage = new Image(getClass().getResource(themePath + "block.png").toExternalForm());

            System.out.println("✅ Images du thème " + currentTheme + " chargées pour l'éditeur");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement images éditeur : " + e.getMessage());
            // Images par défaut en cas d'erreur
            wallImage = null;
            floorImage = null;
            blockImage = null;
        }
    }

    public void setOriginalDimensions(double width, double height) {
        this.originalWidth = width;
        this.originalHeight = height;
        System.out.println("🔍 Dimensions originales sauvegardées : " + width + "x" + height);
    }

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

    private Rectangle createCell(int row, int col) {
        Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
        updateCellAppearance(cell, mapData[row][col]);

        // Style de base
        cell.setStroke(Color.GRAY);
        cell.setStrokeWidth(0.5);

        return cell;
    }

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

    // ✅ MODIFIÉE : setupTools() -> setupDefaults()
    private void setupDefaults() {
        // ✅ SUPPRIMÉ : Configuration des outils radio
        // Plus besoin de listeners pour les outils

        // Nom de map par défaut
        if (userManager.isLoggedIn()) {
            User user = userManager.getCurrentUser();
            mapNameField.setText("Map_" + user.getUsername());
        } else {
            mapNameField.setText("Map_Custom");
        }
    }

    private void setupKeyboardControls() {
        root.setOnKeyPressed(this::handleKeyPress);
        root.setFocusTraversable(true);
        root.requestFocus();
    }

    // ✅ MODIFIÉE : Suppression des contrôles d'outils
    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();

        switch (code) {
            // ✅ SUPPRIMÉ : DIGIT1, DIGIT2, DIGIT3 (plus d'outils à sélectionner)
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

    // ✅ MÉTHODE INCHANGÉE : Logique simplifiée déjà en place
    private void handleCellClick(int row, int col, MouseEvent event) {
        // Ne pas modifier les bordures
        if (row == 0 || row == MAP_HEIGHT - 1 || col == 0 || col == MAP_WIDTH - 1) {
            System.out.println("⚠️ Bordures protégées !");
            return;
        }

        // Protéger les zones de spawn
        if (isSpawnZone(row, col)) {
            System.out.println("⚠️ Zone de spawn protégée !");
            return;
        }

        // ✅ LOGIQUE SIMPLE : Basculer entre vide (0) et mur indestructible (1)
        if (event.isPrimaryButtonDown() || event.getEventType() == MouseEvent.MOUSE_CLICKED) {
            if (mapData[row][col] == 1) {
                // Si c'est un mur indestructible, le supprimer
                mapData[row][col] = 0;
                System.out.println("🗑️ Mur supprimé en (" + row + "," + col + ")");
            } else {
                // Sinon, placer un mur indestructible
                mapData[row][col] = 1;
                System.out.println("🧱 Mur placé en (" + row + "," + col + ")");
            }

            updateCellAppearance(mapCells[row][col], mapData[row][col]);
        }
    }

    private String cellTypeToString(int type) {
        switch (type) {
            case 0: return "Vide";
            case 1: return "Mur";
            case 2: return "Bloc";
            default: return "Inconnu";
        }
    }

    // ✅ MODIFIÉE : Suppression de currentToolLabel
    private void updateInfoPanel() {
        mapSizeLabel.setText("Taille: " + MAP_WIDTH + "x" + MAP_HEIGHT);
        // ✅ SUPPRIMÉ : currentToolLabel.setText("Outil: Vider");
    }

    // ===== ACTIONS DES BOUTONS (INCHANGÉES) =====

    @FXML
    private void saveMap() {
        System.out.println("=== DEBUG SAVE MAP ===");

        String mapName = mapNameField.getText().trim();
        System.out.println("🔍 Nom de la map: '" + mapName + "'");

        if (mapName.isEmpty()) {
            showAlert("Erreur", "Nom de map requis", "Veuillez saisir un nom pour votre map.");
            return;
        }

        try {
            System.out.println("🔍 Vérification maps existantes...");
            List<String> existingMaps = mapManager.getMapsList();
            System.out.println("🔍 Maps existantes: " + existingMaps);
            System.out.println("🔍 Nombre de maps: " + existingMaps.size());

            boolean mapExists = existingMaps.contains(mapName);
            System.out.println("🔍 Map existe déjà? " + mapExists);

            if (mapExists) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Map existante");
                confirm.setHeaderText("Une map avec ce nom existe déjà");
                confirm.setContentText("Voulez-vous la remplacer ?");

                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    return; // Annuler
                }
            }

            System.out.println("🔍 Génération des murs destructibles...");
            generateDestructibleWalls();

            System.out.println("🔍 Création de la matrice...");
            int[][] matrix = createMatrixFromEditor();
            System.out.println("🔍 Matrice créée: " + matrix.length + "x" + (matrix.length > 0 ? matrix[0].length : 0));

            System.out.println("🔍 Obtention de l'auteur...");
            String author = userManager.isLoggedIn() ?
                    userManager.getCurrentUser().getUsername() : "Anonyme";
            System.out.println("🔍 Auteur: " + author);

            System.out.println("🔍 Création de la CustomMap...");
            CustomMap newMap = new CustomMap(mapName, "Map créée avec l'éditeur",
                    MAP_WIDTH, MAP_HEIGHT, matrix, author);
            System.out.println("🔍 CustomMap créée: " + newMap.getName());

            System.out.println("🔍 Validation de la map...");
            if (!newMap.isValid()) {
                showAlert("Erreur", "Map invalide", "La map doit contenir au moins 2 zones de spawn pour les joueurs");
                return;
            }
            System.out.println("🔍 Map valide!");

            System.out.println("🔍 Sauvegarde via MapManager...");
            boolean success = mapManager.saveMap(newMap);
            System.out.println("🔍 Sauvegarde réussie? " + success);

            if (success) {
                showAlert("Succès", "Map sauvegardée",
                        "Map '" + mapName + "' sauvegardée avec succès !\n" +
                                "Total maps : " + mapManager.getMapCount());
                System.out.println("💾 Map sauvegardée: " + mapName);
            } else {
                showAlert("Erreur", "Erreur de sauvegarde", "Impossible de sauvegarder la map");
            }

        } catch (Exception e) {
            System.err.println("❌ EXCEPTION dans saveMap(): " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur de sauvegarde",
                    "Impossible de sauvegarder la map:\n" + e.getMessage());
        }

        System.out.println("=== FIN DEBUG SAVE MAP ===");
    }

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

    private int[][] createMatrixFromEditor() {
        // Utiliser les constantes de l'éditeur pour garantir la bonne taille
        int[][] matrix = new int[MAP_HEIGHT][MAP_WIDTH];

        System.out.println("🔍 Création matrice éditeur: " + MAP_WIDTH + "x" + MAP_HEIGHT);
        System.out.println("🔍 Données éditeur: " + mapData.length + "x" + (mapData.length > 0 ? mapData[0].length : 0));

        for (int row = 0; row < MAP_HEIGHT; row++) {
            for (int col = 0; col < MAP_WIDTH; col++) {
                // Vérifier que nous sommes dans les limites des données de l'éditeur
                if (row < mapData.length && col < mapData[row].length) {
                    matrix[row][col] = mapData[row][col];
                } else {
                    // Remplir avec des murs si hors limites (bordures)
                    matrix[row][col] = 1;
                    System.out.println("⚠️ Remplissage bordure à (" + row + "," + col + ")");
                }
            }
        }

        return matrix;
    }

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

    @FXML
    private void loadMap() {
        List<String> mapNames = mapManager.getMapsList();

        if (mapNames.isEmpty()) {
            showAlert("Info", "Aucune map", "Aucune map sauvegardée trouvée.");
            return;
        }

        // Dialog de sélection
        ChoiceDialog<String> dialog = new ChoiceDialog<>(mapNames.get(0), mapNames);
        dialog.setTitle("Charger une map");
        dialog.setHeaderText("Sélectionnez une map à charger");
        dialog.setContentText("Maps disponibles :");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            loadMapData(result.get());
        }
    }

    private void loadMapData(String mapName) {
        try {
            CustomMap map = mapManager.getMapByName(mapName);
            if (map == null) {
                showAlert("Erreur", "Map introuvable", "Impossible de charger la map '" + mapName + "'");
                return;
            }

            // Charger les données de la map
            mapData = map.getMatrixCopy();
            mapNameField.setText(map.getName());

            // Recréer la grille visuelle
            createMapGrid();

            showAlert("Succès", "Map chargée",
                    "Map '" + map.getName() + "' chargée avec succès !\n" +
                            "Auteur : " + map.getAuthor() + "\n" +
                            "Taille : " + map.getWidth() + "x" + map.getHeight() + "\n" +
                            "Créée le : " + map.getCreated().substring(0, 10));

            System.out.println("📂 Map chargée: " + map.getName());

        } catch (Exception e) {
            showAlert("Erreur", "Erreur de chargement",
                    "Impossible de charger la map:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void clearMap() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Effacer la map");
        confirm.setHeaderText("Êtes-vous sûr ?");
        confirm.setContentText("Cette action effacera toute la map actuelle.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            initializeMapData();
            createMapGrid();
            System.out.println("🗑️ Map effacée");
        }
    }

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

            System.out.println("🏠 Retour au menu avec dimensions : " + originalWidth + "x" + originalHeight);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du retour au menu : " + e.getMessage());
            e.printStackTrace();

            // En cas d'erreur, fermer l'application
            System.exit(1);
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ===== CLASSES DE DONNÉES (INCHANGÉES) =====

    public static class MapData {
        public String name;
        public String author;
        public String theme;
        public int width;
        public int height;
        public String createdDate;
        public int[][] cells;
        public PlayerSpawns playerSpawns;
    }

    public static class PlayerSpawns {
        public Position player1;
        public Position player2;
    }

    public static class Position {
        public int row;
        public int col;

        public Position(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}